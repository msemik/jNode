package pl.edu.uj.engine.eventloop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.ApplicationShutdownEvent;
import pl.edu.uj.engine.CancelJarJobsEvent;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

@Component
public class EventLoopThreadRegistry implements Iterable<EventLoopThread> {

    private Map<Path, EventLoopThread> map = new ConcurrentHashMap<>();

    private Logger logger = LoggerFactory.getLogger(EventLoopThreadRegistry.class);

    @Autowired
    private ApplicationContext context;

    public Optional<EventLoopThread> forJarName(Path jarName) {
        return ofNullable(map.get(jarName));
    }

    public void register(Path jarName, EventLoopThread eventLoopThread) {
        map.put(jarName, eventLoopThread);
    }

    public EventLoopThread unregister(Path jarName) {
        EventLoopThread eventLoopThread = map.remove(jarName);
        return eventLoopThread;
    }

    @EventListener
    public void onCancelJarJobsEvent(CancelJarJobsEvent event) {
        try {
            Path jarFileName = event.getJarFileName();
            Optional<EventLoopThread> optEventLoopThread = forJarName(jarFileName);

            if (!optEventLoopThread.isPresent()) {
                logger.info("There was no EventLoopThread for the jar " + jarFileName + ",  " + toString());
                return;
            }

            logger.info("Forcing EventLoopThread " + jarFileName + " to shutdown");
            EventLoopThread eventLoopThread = optEventLoopThread.get();
            if (eventLoopThread.equals(event.getSource())) {
                //if EventLoopThread pushed that event, we don't need to shutdown it as it is already done.
                return;
            }
            eventLoopThread.shutDown();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    @EventListener
    public void onApplicationShutdown(ApplicationShutdownEvent e) {
        for (EventLoopThread eventLoopThread : this) {
            eventLoopThread.shutDown();
        }
    }

    public EventLoopThread createEventLoopThread(Path jarName) {
        EventLoopThread eventLoopThread = context.getBean(EventLoopThread.class);
        eventLoopThread.startLoop(jarName);
        return eventLoopThread;
    }

    @Override
    public String toString() {
        return "EventLoopThreadRegistry" + map;
    }

    @Override
    public Iterator<EventLoopThread> iterator() {
        return map.values().iterator();
    }

    public Set<Path> jarPaths() {
        return map.keySet();
    }
}
