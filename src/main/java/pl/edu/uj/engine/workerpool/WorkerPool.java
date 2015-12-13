package pl.edu.uj.engine.workerpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import pl.edu.uj.ApplicationShutdownEvent;
import pl.edu.uj.engine.CancelJarJobsEvent;
import pl.edu.uj.engine.eventloop.EventLoopThread;
import pl.edu.uj.engine.eventloop.EventLoopThreadRegistry;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CancellationException;

import static java.lang.String.format;

/**
 * Created by michal on 31.10.15.
 */
@Component
public class WorkerPool {
    private Logger logger = LoggerFactory.getLogger(WorkerPool.class);

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private EventLoopThreadRegistry eventLoopThreadRegistry;

    @Autowired
    private ExecutingTasks executingTasks;

    @EventListener
    public void onShutdownJarJobsEvent(CancelJarJobsEvent event) {
        Path fileName = event.getJarFileName();
        int cancelledJobs = executingTasks.cancelAllRunningJobs(fileName);
        logger.info(format("Cancelled %d jobs for %s, %d jobs left in pool", cancelledJobs, fileName, jobsInPool()));
    }



    public long jobsInPool() {
        return executingTasks.size();
    }

    public void submitTask(WorkerPoolTask task) {
        logger.info("Task " + task.toString() + " is being executed");

        final ListenableFuture<Object> taskResultFuture = taskExecutor.submitListenable(task);
        executingTasks.put(task.getJarName(), taskResultFuture);
        taskResultFuture.addCallback(new ListenableFutureCallback<Object>() {
            @Override
            public void onFailure(Throwable ex) {
                executingTasks.remove(task.getJarName(), taskResultFuture);
                if (ex instanceof CancellationException)
                    return;
                logger.info("Execution of task " + task.toString() + " has failed, reason: " + ex.getMessage());

                Optional<EventLoopThread> eventLoopThread = eventLoopThreadRegistry.forJarName(task.getJarName());
                if (!eventLoopThread.isPresent()) {
                    logger.error("Event loop thread missing for given task: " + task);
                    return;
                }
                eventLoopThread.get().submitTaskFailure(task, ex);
            }

            @Override
            public void onSuccess(Object result) {
                executingTasks.remove(task.getJarName(), taskResultFuture);
                logger.info("Execution of task " + task.toString() + " has been accomplished");

                Optional<EventLoopThread> eventLoopThread = eventLoopThreadRegistry.forJarName(task.getJarName());
                if (!eventLoopThread.isPresent()) {
                    logger.error("Event loop thread missing for given task: " + task);
                    return;
                }
                eventLoopThread.get().submitTaskResult(task, result);
            }
        });
    }

    @EventListener
    public void onApplicationShutdown(ApplicationShutdownEvent e) {
        taskExecutor.shutdown();
    }
}
