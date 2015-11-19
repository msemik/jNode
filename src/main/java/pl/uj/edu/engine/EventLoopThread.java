package pl.uj.edu.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.uj.edu.ApplicationShutdownEvent;
import pl.uj.edu.userlib.Callback;

import javax.annotation.PostConstruct;

@Component
@Scope("prototype")
public class EventLoopThread extends Thread {
    private Logger logger = LoggerFactory.getLogger(EventLoopThread.class);

    private boolean shutdown = false;

    @Autowired
    private ApplicationContext context;

    private EventLoopQueue eventLoopQueue;

    public EventLoopQueue getEventLoopQueue() {
        return eventLoopQueue;
    }

    @PostConstruct
    public void init() {
        eventLoopQueue = context.getBean(EventLoopQueue.class);
    }

    @EventListener
    public void onApplicationShutdown(ApplicationShutdownEvent e) {
        shutdown = true;
    }

    @Override
    public void run() {
        logger.info("Started to listen for tasks results");

        while (true) {
            if (shutdown)
                return;

            try {
                EventLoopRespond eventLoopRespond = eventLoopQueue.take();

                if (eventLoopRespond.getType() == EventLoopRespondType.POISON) {
                    logger.info("Received poison, aborting.");
                    return;
                }

                Callback callback = eventLoopRespond.getCallback();

                if (eventLoopRespond.getType() == EventLoopRespondType.SUCCESS) {
                    logger.info("Received task result, executing callback");
                    Object taskResult = eventLoopRespond.getTaskResult();
                    callback.onSuccess(taskResult);
                } else {
                    logger.info("Received task exception, executing callback");
                    Throwable exception = eventLoopRespond.getException();
                    callback.onFailure(exception);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
