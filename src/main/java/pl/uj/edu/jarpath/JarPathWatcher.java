package pl.uj.edu.jarpath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.uj.edu.ApplicationShutdownEvent;

import javax.annotation.PostConstruct;
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

    @Autowired
    private JarPathServices jarServices;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private Path path;

    private boolean shutDown = false;

    @PostConstruct
    public void watch() {
        path = jarServices.getJarPath();
        start();
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

                        if (ENTRY_CREATE == kind) {
                            if (!jarServices.isJarOrProperty(eventPath))
                                continue;
                            jarServices.validateReadWriteAccess(eventPath);

                            logger.info("File created: " + eventPath);
                            if (jarServices.isJar(eventPath))
                                eventPublisher.publishEvent(new JarReceivedEvent(eventPath, this));

                        } else if (ENTRY_DELETE == kind) {
                            if (!eventPath.toString().endsWith(".jar") && !eventPath.toString().endsWith(".properties"))
                                continue;
                            logger.info("File deleted: " + eventPath);

                            if (jarServices.isJar(eventPath))
                                eventPublisher.publishEvent(new JarDeletedEvent(eventPath, this));
                            else if (jarServices.isProperties(path)) {
                                Optional<Path> jarForProperty = jarServices.getJarForProperty(path);
                                if (!jarForProperty.isPresent())
                                    continue;

                                jarServices.validateReadWriteAccess(jarForProperty.get());

                                eventPublisher.publishEvent(new JarPropertiesDeletedEvent(path, this));
                            }

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
     * WatchService returns variable amount of events per path.
     * These may contain duplicate events, mix of events.
     * When you create a file there is a mix of create + edit events, function will reduce it to create event
     * When you delete file there is only a delete event, function will return delete event
     * When you override file, i couldn't find any pattern. Various things happened. create event will be returned.
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
                reducedEvents.add(watchEvent);
                continue;
            }
            if (kinds.size() == 3) {
                if (watchEvent.kind() == ENTRY_CREATE)
                    reducedEvents.add(watchEvent);
                continue;
            }
            if (watchEvent.kind() != ENTRY_MODIFY)
                reducedEvents.add(watchEvent);
        }

        return reducedEvents;
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