package pl.uj.edu.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import pl.uj.edu.ApplicationShutdownEvent;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

/**
 * Created by michal on 31.10.15.
 */
@Component
public class WorkerPool {
    private Logger logger = LoggerFactory.getLogger(WorkerPool.class);

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private CallbackStorage callbackStorage;

    @Autowired
    private EventLoopQueue eventLoopQueue;

    public void submitTask(Task task) {
        logger.info("Task " + task.toString() + " is being executed");

        ListenableFuture<Object> taskResultFuture = taskExecutor.submitListenable(task);
        taskResultFuture.addCallback(new ListenableFutureCallback<Object>() {
            @Override
            public void onFailure(Throwable ex) {
                logger.error("Execution of task " + task.toString() + " has failed, reason: " + ex.getMessage());

                Callback callback = callbackStorage.remove(task);

                try {
                    eventLoopQueue.put(new EventLoopRespond(EventLoopRespondType.FAILURE, callback, ex));
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }

            @Override
            public void onSuccess(Object result) {
                logger.info("Execution of task " + task.toString() + " has been accomplished");

                Callback callback = callbackStorage.remove(task);

                try {
                    eventLoopQueue.put(new EventLoopRespond(EventLoopRespondType.SUCCESS, callback, result));
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }
        });
    }

    @EventListener
    public void onApplicationShutdown(ApplicationShutdownEvent e) {
        taskExecutor.shutdown();
    }
}
