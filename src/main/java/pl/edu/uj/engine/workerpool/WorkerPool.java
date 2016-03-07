package pl.edu.uj.engine.workerpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import pl.edu.uj.ApplicationShutdownEvent;
import pl.edu.uj.crosscuting.ReflectionUtils;
import pl.edu.uj.engine.event.CancelJarJobsEvent;
import pl.edu.uj.engine.event.TaskFinishedEvent;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.FutureTask;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Created by michal on 31.10.15.
 */
@Component
public class WorkerPool {
    private Logger logger = LoggerFactory.getLogger(WorkerPool.class);
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ExecutingTasks executingTasks;
    private ThreadPoolTaskExecutor taskExecutor;
    private BlockingQueue<Runnable> queue;

    @EventListener
    public void on(ApplicationShutdownEvent e) {
        if (taskExecutor != null)
            taskExecutor.shutdown();
    }

    @EventListener
    public void on(CancelJarJobsEvent event) {
        Path fileName = event.getJarFileName();
        int cancelledJobs = executingTasks.cancelAllRunningJobs(fileName);
        logger.info(format("Cancelled %d jobs for %s, %d jobs left in pool", cancelledJobs, fileName, jobsInPool()));
    }

    public void submitTask(WorkerPoolTask task, boolean silently) {
        logger.info("Task " + task.toString() + " is being executed");

        ThreadPoolTaskExecutor executor = getTaskExecutor();
        final ListenableFuture<Object> taskResultFuture = executor.submitListenable(task);
        executingTasks.put(task.getJarName(), taskResultFuture);
        taskResultFuture.addCallback(new ListenableFutureCallback<Object>() {
            @Override
            public void onFailure(Throwable ex) {
                executingTasks.remove(task.getJarName(), taskResultFuture);
                if (ex instanceof CancellationException)
                    return;
                logger.info("Execution of task " + task.toString() + " has failed, reason: " + ex.getMessage());
                eventPublisher.publishEvent(new TaskFinishedEvent(this, TaskFinishedEvent.TaskFinalExecutionStatus.FAILURE, task, ex));
            }

            @Override
            public void onSuccess(Object result) {
                executingTasks.remove(task.getJarName(), taskResultFuture);
                logger.info("Execution of task " + task.toString() + " has been accomplished");
                if (!silently)
                    eventPublisher.publishEvent(new TaskFinishedEvent(this, TaskFinishedEvent.TaskFinalExecutionStatus.SUCCESS, task, result));
            }
        });

        int corePoolSize = executor.getCorePoolSize();
        int tasksInPool = executingTasks.size();
        logger.debug("Pool size: " + corePoolSize + " , tasksInPool: " + tasksInPool);
        if (corePoolSize - tasksInPool < 0) {
            eventPublisher.publishEvent(new WorkerPoolOverflowEvent(this));
        }
    }

    private ThreadPoolTaskExecutor getTaskExecutor() {
        if (taskExecutor == null)
            taskExecutor = applicationContext.getBean(ThreadPoolTaskExecutor.class);
        return taskExecutor;
    }

    public long jobsInPool() {
        return executingTasks.size();
    }

    public long poolSize() {
        return getTaskExecutor().getCorePoolSize();
    }

    public Optional<WorkerPoolTask> pollTask() {
        Runnable polledItem = getAwaitingTasks().poll();
        if (polledItem == null) {
            return empty();
        }
        if (!(polledItem instanceof FutureTask)) {
            String message = "Unexpected object type pulled from executors queue: " + polledItem.getClass().getCanonicalName();
            throw new AssertionError(message);
        }
        FutureTask<Callable> futureTask = (FutureTask<Callable>) polledItem;
        executingTasks.remove(futureTask);
        if (futureTask.isDone()) { //isDone <=> futureTask.state != NEW (executing started, not really expected here)
            String message = join("\n"
                    , "Prepare for a nice day because you really did a bad thing."
                    , "I mean taking out this task from internal executor queue."
                    , "Unfortunately its started executing, not as you expected");
            throw new AssertionError(message);
        }
        return of((WorkerPoolTask) ReflectionUtils.readFieldValue(FutureTask.class, futureTask, "callable"));
    }

    private BlockingQueue<Runnable> getAwaitingTasks() {
        if (queue == null) {
            queue = getTaskExecutor().getThreadPoolExecutor().getQueue();
        }
        return queue;
    }

    public void submitTask(WorkerPoolTask workerPoolTask) {
        submitTask(workerPoolTask, false);
    }

    public boolean hasAvailableThreads() {
        return poolSize() > jobsInPool();
    }
}
