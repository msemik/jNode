package pl.edu.uj.engine.eventloop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.engine.events.CancelJarJobsEvent;
import pl.edu.uj.jarpath.Jar;
import pl.edu.uj.main.ApplicationShutdownEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;

@Component
public class EventLoopThreadRegistry {
    private Logger logger = LoggerFactory.getLogger(EventLoopThreadRegistry.class);
    private Map<Jar, EventLoopThread> map = new HashMap<>();
    @Autowired
    private ApplicationContext context;

    @EventListener
    public void onCancelJarJobsEvent(CancelJarJobsEvent event) {
        try {
            Jar jar = event.getJar();
            EventLoopThread eventLoopThread = remove(jar);

            if (eventLoopThread == null) {
                logger.info("There was no EventLoopThread for the jar " + jar + ",  " + toString());
                return;
            }

            logger.info("Forcing EventLoopThread " + jar + " to shutdown");
            if (eventLoopThread.equals(event.getSource())) {
                //if EventLoopThread pushed that events, we don't need to shutdown it as it is already done.
                return;
            }
            eventLoopThread.shutDown();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    public synchronized EventLoopThread remove(Jar jar) {
        return map.remove(jar);
    }

    @Override
    public String toString() {
        return "EventLoopThreadPool" + map;
    }

    @EventListener
    public synchronized void onApplicationShutdown(ApplicationShutdownEvent e) {
        map.values().stream().forEach(EventLoopThread::shutDown);
    }

    public synchronized Optional<EventLoopThread> get(Jar jar) {
        return ofNullable(map.get(jar));
    }

    public synchronized EventLoopThread getOrCreate(Jar jar) {
        return map.computeIfAbsent(jar, this::createEventLoopThread);
    }

    private EventLoopThread createEventLoopThread(Jar jar) {
        EventLoopThread eventLoopThread = context.getBean(EventLoopThread.class);
        eventLoopThread.startLoop(jar);
        return eventLoopThread;
    }

    public synchronized Set<Jar> getJars() {
        return map.keySet();
    }
}
