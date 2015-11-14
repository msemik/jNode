package pl.uj.edu.engine;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.uj.edu.ApplicationShutdownEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by alanhawrot on 14.11.2015.
 */
@Component
public class EventLoopQueue {
    private BlockingQueue<EventLoopRespond> eventLoopResponds = new LinkedBlockingQueue<>();

    public void put(EventLoopRespond eventLoopRespond) throws InterruptedException {
        eventLoopResponds.put(eventLoopRespond);
    }

    public EventLoopRespond take() throws InterruptedException {
        return eventLoopResponds.take();
    }

    @EventListener
    public void onApplicationShutdown(ApplicationShutdownEvent e) {
        eventLoopResponds.add(new EventLoopRespond(EventLoopRespondType.POISON));
    }
}
