package pl.uj.edu.engine.eventloop;

import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.uj.edu.ApplicationShutdownEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static pl.uj.edu.engine.eventloop.EventLoopResponseType.POISON;

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
        eventLoopResponses.add(new EventLoopResponse(POISON));
    }

    public boolean isEmpty() {
        return eventLoopResponses.isEmpty();
    }
}
