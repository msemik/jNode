package pl.edu.uj.engine.eventloop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.ApplicationShutdownEvent;
import pl.edu.uj.engine.event.CancelJarJobsEvent;
import pl.edu.uj.jarpath.Jar;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Component
public class EventLoopThreadPool {
    private Logger logger = LoggerFactory.getLogger(EventLoopThreadPool.class);
    private Map<Jar, EventLoopThreadPoolEntry> map = new HashMap<>();
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
                //if EventLoopThread pushed that event, we don't need to shutdown it as it is already done.
                return;
            }
            eventLoopThread.shutDown();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }

    public synchronized EventLoopThread remove(Jar jar) {
        EventLoopThreadPoolEntry entry = map.remove(jar);
        if (entry == null) {
            return null;
        }
        return entry.getEventLoopThread();
    }

    @Override
    public String toString() {
        return "EventLoopThreadPool" + map;
    }

    public synchronized Optional<EventLoopThread> get(Jar jar) {
        EventLoopThreadPoolEntry entry = map.get(jar);
        if (entry == null) {
            return empty();
        }
        return ofNullable(map.get(jar).getEventLoopThread());
    }

    public synchronized long returnEventLoopThread(Jar jar) {
        EventLoopThreadPoolEntry entry = map.get(jar);
        if (entry == null) {
            return 0;
        }
        long eventLoopRequestCounter = entry.decrementAndGetRequestCounter();
        if (eventLoopRequestCounter == 0) {
            remove(jar);
        }
        return eventLoopRequestCounter;
    }

    @EventListener
    public synchronized void onApplicationShutdown(ApplicationShutdownEvent e) {
        map.values().stream().map(EventLoopThreadPoolEntry::getEventLoopThread).forEach(EventLoopThread::shutDown);
    }

    public synchronized EventLoopThread takeOrCreate(Jar jar) {
        EventLoopThreadPoolEntry entry = map.computeIfAbsent(jar, jar0 -> new EventLoopThreadPoolEntry(createEventLoopThread(jar0)));
        entry.incrementAndGetRequestCounter();
        return entry.getEventLoopThread();
    }

    private EventLoopThread createEventLoopThread(Jar jar) {
        EventLoopThread eventLoopThread = context.getBean(EventLoopThread.class);
        eventLoopThread.startLoop(jar);
        return eventLoopThread;
    }

    public synchronized Optional<EventLoopThread> take(Jar jar) {
        EventLoopThreadPoolEntry entry = map.get(jar);
        if (entry == null) {
            return empty();
        }
        entry.incrementAndGetRequestCounter();
        return ofNullable(entry.getEventLoopThread());
    }

    public synchronized Set<Jar> getJars() {
        return map.keySet();
    }
}
