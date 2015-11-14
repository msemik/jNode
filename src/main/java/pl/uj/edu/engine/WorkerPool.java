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
import pl.uj.edu.userlib.TaskResult;

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

        ListenableFuture<TaskResult> taskResultFuture = taskExecutor.submitListenable(task);
        taskResultFuture.addCallback(new ListenableFutureCallback<TaskResult>() {
            @Override
            public void onFailure(Throwable ex) {
                logger.error("Execution of task " + task.toString() + " has failed, reason: " + ex.getMessage());

                callbackStorage.remove(task);
                try {
                    eventLoopQueue.put(new EventLoopRespond(EventLoopRespondType.FAILURE));
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }

            @Override
            public void onSuccess(TaskResult result) {
                try {
                    logger.info("Execution of task " + task.toString() + " has been accomplished");

                    Callback callback = callbackStorage.remove(task);
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
