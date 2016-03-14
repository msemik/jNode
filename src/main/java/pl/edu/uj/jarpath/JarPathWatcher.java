package pl.edu.uj.jarpath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.ApplicationInitializedEvent;
import pl.edu.uj.ApplicationShutdownEvent;
import pl.edu.uj.crosscuting.OSValidator;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by michal on 22.10.15.
 */
@Component
public class JarPathWatcher extends Thread {
    private static final long SCAN_PERIOD = 1000; //1 second
    private Logger logger = LoggerFactory.getLogger(JarPathWatcher.class);
    private Path path;
    private boolean shutDown = false;
    @Autowired
    private JarPathServices jarServices;
    @Autowired
    private JarPathManager jarPathManager;
    @Autowired
    private OSValidator osValidator;
    @Autowired
    private JarFactory jarFactory;

    @EventListener
    public void watch(ApplicationInitializedEvent event) {
        path = jarServices.getJarPath();
        start();
        try {
            Files.walk(path).forEach(filePath -> {
                if (jarServices.isValidExistingJar(filePath))
                    jarPathManager.onFoundJarAfterStart(jarFactory.getFor(filePath));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @EventListener
    public void applicationShutdown(ApplicationShutdownEvent event) {
        shutDown = true;
    }

    @Override
    public void run() {
        try {
            logger.info("Watching jar path '" + path + "'");

            FileSystem fs = path.getFileSystem();

            try (WatchService service = fs.newWatchService()) {

                path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);


                while (true) {
                    if (shutDown)
                        return;

                    WatchKey key = service.poll(SCAN_PERIOD, TimeUnit.MILLISECONDS);
                    if (key == null)
                        continue;

                    List<WatchEvent<?>> watchEvents = reducePathsToOnlySingleEventForPath(key);
                    for (WatchEvent<?> watchEvent : watchEvents) {
                        Kind<?> kind = watchEvent.kind();

                        if (kind == OVERFLOW) {
                            logger.warn("Watch event overflow");
                            continue;
                        }

                        Path eventPath = ((WatchEvent<Path>) watchEvent).context();
                        eventPath = path.resolve(eventPath);

                        Kind<Path> createdFileWithContent = getEventAppearingWhenCopyingFileHasFinished();
                        if (createdFileWithContent == kind) {
                            if (!jarServices.isJarOrProperty(eventPath))
                                continue;
                            jarServices.validateReadWriteAccess(eventPath);

                            logger.info("File created: " + eventPath);
                            if (jarServices.isValidExistingJar(eventPath))
                                jarPathManager.onCreateJar(jarFactory.getFor(eventPath));

                        } else if (ENTRY_DELETE == kind) {
                            if (!eventPath.toString().endsWith(".jar") && !eventPath.toString().endsWith(".properties"))
                                continue;
                            logger.info("File deleted: " + eventPath);

                            if (eventPath.toString().endsWith(".jar"))
                                jarPathManager.onDeleteJar(jarFactory.getFor(eventPath));
                            else
                                jarPathManager.onDeleteProperties(eventPath);

                        }
                    }
                    if (!key.reset()) {
                        logger.info("key state is not valid (reset() returned true)");
                        break;
                    }

                    Thread.sleep(SCAN_PERIOD);
                }

            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * WatchService returns variable amount of events per paths.
     * These may contain duplicate events, or mix of events like CREATE, MODIFY.
     * When you create a file in Ubuntu there is single CREATE event(in that time file is empty).
     * When you create file in mac, there is single CREATE event and file has already content in it.
     * When file is filled there will be event MODIFY (or it might be CREATE+MODIFY).
     * When you delete file there is only a delete event, function will return DELETE event
     * <p/>
     * This function should reduce pain to analise all these combinations in later stages.
     * We ignore signals about empty file and wait until it will be filled, returning only single ENTRY_MODIFY event.
     */
    private List<WatchEvent<?>> reducePathsToOnlySingleEventForPath(WatchKey key) {
        List<WatchEvent<?>> watchEvents = key.pollEvents();

        Map<Path, Set<Kind<Path>>> pathEventKinds = groupEventsByPaths(watchEvents);

        List<WatchEvent<?>> reducedEvents = new ArrayList<>();
        for (WatchEvent<?> watchEvent : watchEvents) {
            if (watchEvent.kind() == OVERFLOW) {
                reducedEvents.add(watchEvent);
                continue;
            }
            Path path = (Path) watchEvent.context();
            Set<Kind<Path>> kinds = pathEventKinds.get(path);
            logger.debug(path + " with event kinds: " + kinds);
            if (kinds.size() == 1) {
                if (osValidator.isUnix() && watchEvent.kind() == ENTRY_CREATE) //ignore empty file on linux
                    continue;
                reducedEvents.add(watchEvent);
                continue;
            }

            if (kinds.size() == 3 || kinds.size() == 2) {
                if (watchEvent.kind() == ENTRY_MODIFY)
                    reducedEvents.add(watchEvent);
                continue;
            }
        }

        return reducedEvents;
    }

    public Kind<Path> getEventAppearingWhenCopyingFileHasFinished() {
        if (osValidator.isUnix())
            return ENTRY_MODIFY;
        return ENTRY_CREATE;
    }

    private Map<Path, Set<Kind<Path>>> groupEventsByPaths(List<WatchEvent<?>> watchEvents) {
        Map<Path, Set<Kind<Path>>> pathEventKinds = new HashMap<>();
        for (WatchEvent<?> watchEvent : watchEvents) {
            Kind<?> kind = watchEvent.kind();
            if (kind == OVERFLOW)
                continue;
            Path path = (Path) watchEvent.context();
            Set<Kind<Path>> kinds = pathEventKinds.getOrDefault(path, new HashSet<Kind<Path>>());
            kinds.add((Kind<Path>) kind);
            pathEventKinds.putIfAbsent(path, kinds);
        }
        return pathEventKinds;
    }
}