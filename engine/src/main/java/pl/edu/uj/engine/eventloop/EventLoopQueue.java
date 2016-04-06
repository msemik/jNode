package pl.edu.uj.engine.eventloop;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by alanhawrot on 14.11.2015.
 */
@Component
@Scope("prototype")
public class EventLoopQueue {
    public static final EventLoopResponse POISON = new EventLoopResponse(EventLoopResponse.Type.POISON);
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
            } catch (InterruptedException | IllegalMonitorStateException e) {
                ;
                //IllegalMonitorStateException may be thrown due to usage of Thread.stop() method which releases all locks.
                return POISON;
            }
        }
    }

    public boolean isEmpty() {
        return eventLoopResponses.isEmpty();
    }
}
