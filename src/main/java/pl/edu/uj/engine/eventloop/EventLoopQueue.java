package pl.edu.uj.engine.eventloop;

import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.ApplicationShutdownEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by alanhawrot on 14.11.2015.
 */
@Component
@Scope("prototype")
public class EventLoopQueue {
    private BlockingQueue<EventLoopResponse> eventLoopResponses = new LinkedBlockingQueue<>();

    public void put(EventLoopResponse eventLoopResponse) {
        while (true) {
            try {
                eventLoopResponses.put(eventLoopResponse);
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public EventLoopResponse take() {
        while (true) {
            try {
                return eventLoopResponses.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @EventListener
    public void onApplicationShutdown(ApplicationShutdownEvent e) {
        poison();
    }

    public void poison() {
        eventLoopResponses.add(new EventLoopResponse(EventLoopResponseType.POISON));
    }

    public boolean isEmpty() {
        return eventLoopResponses.isEmpty();
    }
}