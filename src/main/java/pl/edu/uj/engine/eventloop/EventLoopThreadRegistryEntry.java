package pl.edu.uj.engine.eventloop;

/**
 * Created by alanhawrot on 21.03.2016.
 */
public class EventLoopThreadRegistryEntry {
    private long requestCounter = 0;
    private EventLoopThread eventLoopThread;

    public EventLoopThreadRegistryEntry(EventLoopThread eventLoopThread) {
        this.eventLoopThread = eventLoopThread;
    }

    public EventLoopThread getEventLoopThread() {
        return eventLoopThread;
    }

    public long getRequestCounter() {
        return requestCounter;
    }

    public long incrementAndGetRequestCounter() {
        return ++requestCounter;
    }

    public long decrementAndGetRequestCounter() {
        return --requestCounter;
    }
}
