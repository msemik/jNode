package pl.uj.edu.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.uj.edu.ApplicationShutdownEvent;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.TaskResult;

import javax.annotation.PostConstruct;

@Component
public class EventLoopThread extends Thread {
    private Logger logger = LoggerFactory.getLogger(EventLoopThread.class);

    private boolean shutdown = false;

    @Autowired
    private EventLoopQueue eventLoopQueue;

    @PostConstruct
    public void startThread() {
        start();
    }

    @EventListener
    public void onApplicationShutdown(ApplicationShutdownEvent e) {
        shutdown = true;
    }

    @Override
    public void run() {
        logger.info("EventLoopThread is waiting for finished tasks");

        while (true) {
            if (shutdown)
                return;

            try {
                EventLoopRespond eventLoopRespond = eventLoopQueue.take();

                if (eventLoopRespond.getType() != EventLoopRespondType.POISON) {
                    Callback callback = eventLoopRespond.getCallback();
                    TaskResult taskResult = eventLoopRespond.getTaskResult();

                    logger.info("EventLoopThread is doing a callback " + callback.toString());

                    if (eventLoopRespond.getType() == EventLoopRespondType.SUCCESS) {
                        callback.onSuccess(taskResult);
                    } else {
                        callback.onFailure();
                    }
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
