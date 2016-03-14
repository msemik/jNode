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

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

@Component
public class EventLoopThreadRegistry implements Iterable<EventLoopThread> {

    private Map<Jar, EventLoopThread> map = new ConcurrentHashMap<>();

    private Logger logger = LoggerFactory.getLogger(EventLoopThreadRegistry.class);

    @Autowired
    private ApplicationContext context;

    public void register(Jar jar, EventLoopThread eventLoopThread) {
        map.put(jar, eventLoopThread);
    }

    public EventLoopThread unregister(Jar jar) {
        EventLoopThread eventLoopThread = map.remove(jar);
        return eventLoopThread;
    }

    @EventListener
    public void onCancelJarJobsEvent(CancelJarJobsEvent event) {
        try {
            Jar jar = event.getJar();
            Optional<EventLoopThread> optEventLoopThread = forJar(jar);

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

    public Optional<EventLoopThread> forJar(Jar jar) {
        return ofNullable(map.get(jar));
    }

    @Override
    public String toString() {
        return "EventLoopThreadRegistry" + map;
    }

    @EventListener
    public void onApplicationShutdown(ApplicationShutdownEvent e) {
        for (EventLoopThread eventLoopThread : this) {
            eventLoopThread.shutDown();
        }
    }

    public EventLoopThread createEventLoopThread(Jar jar) {
        EventLoopThread eventLoopThread = context.getBean(EventLoopThread.class);
        eventLoopThread.startLoop(jar);
        return eventLoopThread;
    }

    @Override
    public Iterator<EventLoopThread> iterator() {
        return map.values().iterator();
    }

    public Set<Jar> getJars() {
        return map.keySet();
    }
}
