package pl.edu.uj.engine.eventloop;

/**
 * Created by alanhawrot on 21.03.2016.
 */
public class EventLoopThreadPoolEntry {
    private long requestCounter = 0;
    private EventLoopThread eventLoopThread;

    public EventLoopThreadPoolEntry(EventLoopThread eventLoopThread) {
        this.eventLoopThread = eventLoopThread;
    }

    public EventLoopThread getEventLoopThread() {
        return eventLoopThread;
    }

    public long incrementAndGetRequestCounter() {
        return ++requestCounter;
    }

    public long decrementAndGetRequestCounter() {
        return --requestCounter;
    }
}
