package pl.edu.uj.jnode.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import pl.edu.uj.jnode.engine.event.CancelJarJobsEvent;
import pl.edu.uj.jnode.engine.event.TaskFinishedEvent;
import pl.edu.uj.jnode.engine.event.TaskReceivedEvent;
import pl.edu.uj.jnode.engine.eventloop.EventLoopThread;
import pl.edu.uj.jnode.engine.eventloop.EventLoopThreadRegistry;
import pl.edu.uj.jnode.engine.workerpool.MainClassWorkerPoolTask;
import pl.edu.uj.jnode.engine.workerpool.WorkerPool;
import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.jarpath.JarDeletedEvent;
import pl.edu.uj.jnode.jarpath.JarPropertiesDeletedEvent;
import pl.edu.uj.jnode.jarpath.JarStateChangedEvent;
import pl.edu.uj.jnode.userlib.Callback;

import java.util.Optional;

import static pl.edu.uj.jnode.jarpath.JarExecutionState.NOT_STARTED;

@Component
public class TaskCoordinator {
    private Logger logger = LoggerFactory.getLogger(TaskCoordinator.class);
    @Autowired
    private WorkerPool workerPool;
    @Autowired
    private EventLoopThreadRegistry eventLoopThreadRegistry;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    public void on(JarStateChangedEvent event) {
        Jar jar = event.getJar();
        logger.info("Got jar " + jar + " with properties " + event.getProperties());
        if (event.getProperties().getExecutionState() != NOT_STARTED) {
            return;
        }
        startJarJob(jar);
    }

    private void startJarJob(Jar jar) {
        logger.info("Launching main class for jar " + jar);
        EventLoopThread eventLoopThread = eventLoopThreadRegistry.getOrCreate(jar);
        MainClassWorkerPoolTask task = new MainClassWorkerPoolTask(jar);
        eventLoopThread.registerTask(task, EmptyCallback.INSTANCE);
        workerPool.submitTask(task);
    }

    @EventListener
    public void on(JarDeletedEvent event) {
        eventPublisher.publishEvent(new CancelJarJobsEvent(this, event.getJar()));
    }

    @EventListener
    public void on(JarPropertiesDeletedEvent event) {
        Jar jar = event.getJar();
        if (eventLoopThreadRegistry.get(jar).isPresent()) {
            eventPublisher.publishEvent(new CancelJarJobsEvent(this, jar));
        } else {
            startJarJob(jar);
        }
    }

    @EventListener
    public void on(TaskReceivedEvent event) {
        WorkerPoolTask task = event.getTask();
        Callback callback = event.getCallback();
        if (!task.isExternal()) {
            logger.info("Saving callback " + callback + " in EventLoopThread for task " + task);
            Optional<EventLoopThread> eventLoopThread = eventLoopThreadRegistry.get(task.getJar());
            if (!eventLoopThread.isPresent()) {
                logger.error("Event loop thread is missing when received task: " + task + " " + eventLoopThreadRegistry);
                return;
            }
            eventLoopThread.get().registerTask(task, callback);
        }
        logger.info("Submitting newly received task " + task + " to pool");
        workerPool.submitTask(task);
    }

    @EventListener
    public void on(TaskFinishedEvent event) {
        WorkerPoolTask task = event.getTask();
        if (task.isExternal()) {
            return;
        }
        Optional<EventLoopThread> eventLoopThread = eventLoopThreadRegistry.get(task.getJar());
        if (!eventLoopThread.isPresent()) {
            logger.error("Event loop thread missing for given task: " + task);
            return;
        }
        if (event.isSuccess()) {
            eventLoopThread.get().submitTaskResult(task, event.getTaskResultOrException());
        } else {
            eventLoopThread.get().submitTaskFailure(task, (Throwable) event.getTaskResultOrException());
        }
    }
}
