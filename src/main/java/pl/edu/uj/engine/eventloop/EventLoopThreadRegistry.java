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

import java.util.*;

import static java.util.Optional.*;

@Component
public class EventLoopThreadRegistry implements Iterable<EventLoopThread> {
    private Logger logger = LoggerFactory.getLogger(EventLoopThreadRegistry.class);
    private Map<Jar, EventLoopThreadRegistryEntry> map = new HashMap<>();

    @Autowired
    private ApplicationContext context;

    public synchronized Optional<EventLoopThread> unregister(Jar jar) {
        EventLoopThreadRegistryEntry entry = map.get(jar);
        return entry.getRequestCounter() == 0 ? of(map.remove(jar).getEventLoopThread()) : empty();
    }

    @EventListener
    public void onCancelJarJobsEvent(CancelJarJobsEvent event) {
        try {
            Jar jar = event.getJar();
            Optional<EventLoopThread> optEventLoopThread = get(jar);

            if (!optEventLoopThread.isPresent()) {
                logger.info("There was no EventLoopThread for the jar " + jar + ",  " + toString());
                return;
            }

            logger.info("Forcing EventLoopThread " + jar + " to shutdown");
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

    public synchronized Optional<EventLoopThread> get(Jar jar) {
        EventLoopThreadRegistryEntry entry = map.get(jar);
        entry.incrementAndGetRequestCounter();
        return ofNullable(entry.getEventLoopThread());
    }

    @Override
    public String toString() {
        return "EventLoopThreadRegistry" + map;
    }

    public synchronized long returnEventLoopThread(Jar jar) {
        return map.get(jar).decrementAndGetRequestCounter();
    }

    @EventListener
    public void onApplicationShutdown(ApplicationShutdownEvent e) {
        for (EventLoopThread eventLoopThread : this) {
            eventLoopThread.shutDown();
        }
    }

    public synchronized EventLoopThread getOrCreate(Jar jar) {
        EventLoopThreadRegistryEntry entry = map.computeIfAbsent(jar, jar0 -> new EventLoopThreadRegistryEntry(createWithoutRegistration(jar0)));
        entry.incrementAndGetRequestCounter();
        return entry.getEventLoopThread();
    }

    private EventLoopThread createWithoutRegistration(Jar jar) {
        EventLoopThread eventLoopThread = context.getBean(EventLoopThread.class);
        eventLoopThread.startLoop(jar);
        return eventLoopThread;
    }

    public EventLoopThread create(Jar jar) {
        EventLoopThread eventLoopThread = createWithoutRegistration(jar);
        register(jar, eventLoopThread);
        return eventLoopThread;
    }

    private synchronized void register(Jar jar, EventLoopThread eventLoopThread) {
        EventLoopThreadRegistryEntry entry = new EventLoopThreadRegistryEntry(eventLoopThread);
        entry.incrementAndGetRequestCounter();
        map.put(jar, entry);
    }

    @Override
    public Iterator<EventLoopThread> iterator() {
        return map.values().stream().map(EventLoopThreadRegistryEntry::getEventLoopThread).iterator();
    }

    public Set<Jar> getJars() {
        return map.keySet();
    }
}
