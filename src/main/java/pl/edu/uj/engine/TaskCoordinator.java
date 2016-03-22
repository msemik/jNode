package pl.edu.uj.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.engine.event.CancelJarJobsEvent;
import pl.edu.uj.engine.event.NewTaskReceivedEvent;
import pl.edu.uj.engine.event.TaskFinishedEvent;
import pl.edu.uj.engine.eventloop.EventLoopThread;
import pl.edu.uj.engine.eventloop.EventLoopThreadPool;
import pl.edu.uj.engine.workerpool.MainClassWorkerPoolTask;
import pl.edu.uj.engine.workerpool.WorkerPool;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jarpath.*;
import pl.uj.edu.userlib.Callback;

import java.util.Optional;

import static pl.edu.uj.jarpath.JarExecutionState.NOT_STARTED;

@Component
public class TaskCoordinator {
    private Logger logger = LoggerFactory.getLogger(TaskCoordinator.class);
    @Autowired
    private WorkerPool workerPool;
    @Autowired
    private EventLoopThreadPool eventLoopThreadPool;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onJarStateChanged(JarStateChangedEvent event) {
        Jar jar = event.getJar();

        logger.info("Got jar " + jar + " with properties " + event.getProperties());

        if (event.getProperties().getExecutionState() != NOT_STARTED) {
            return;
        }

        startJarJob(jar);
    }

    private void startJarJob(Jar jar) {
        logger.info("Launching main class for jar " + jar);

        EventLoopThread eventLoopThread = eventLoopThreadPool.takeOrCreate(jar);
        MainClassWorkerPoolTask task = new MainClassWorkerPoolTask(jar);
        EmptyCallback callback = new EmptyCallback();
        eventLoopThread.registerTask(task, callback);
        workerPool.submitTask(task);
    }

    @EventListener
    public void onJarDeleted(JarDeletedEvent event) {
        eventPublisher.publishEvent(new CancelJarJobsEvent(this, event.getJar()));
    }

    @EventListener
    public void onJarPropertiesDeleted(JarPropertiesDeletedEvent event) {
        Jar jar = event.getJar();
        if (eventLoopThreadPool.get(jar).isPresent()) {
            eventPublisher.publishEvent(new CancelJarJobsEvent(this, jar));
        } else {
            startJarJob(jar);
        }
    }

    @EventListener
    public void onNewTaskReceived(NewTaskReceivedEvent event) {
        WorkerPoolTask task = event.getTask();
        Callback callback = event.getCallback();

        logger.info("Submitting newly received task to pool and saving callback in EventLoopThread " + task);

        EventLoopThread eventLoopThread;
        Jar jar = task.getJar();
        if (task.isExternal()) { //might be recreated for external tasks.
            eventLoopThread = eventLoopThreadPool.takeOrCreate(jar);
        } else {
            eventLoopThread = eventLoopThreadPool.take(jar).orElse(null);
            if (eventLoopThread == null) {
                logger.error("Event loop thread is missing when received task: " + task + " " + eventLoopThreadPool);
                return;
            }
        }
        eventLoopThread.registerTask(task, callback);
        workerPool.submitTask(task);
    }

    @EventListener
    public void onTaskFinished(TaskFinishedEvent event) {
        WorkerPoolTask task = event.getTask();

        Optional<EventLoopThread> eventLoopThread = eventLoopThreadPool.get(task.getJar());
        if (!eventLoopThread.isPresent()) {
            logger.error("Event loop thread missing for given task: " + task);
            return;
        }

        if (event.withSuccess()) {
            eventLoopThread.get().submitTaskResult(task, event.getTaskResultOrException());
        } else {
            eventLoopThread.get().submitTaskFailure(task, (Throwable) event.getTaskResultOrException());
        }
    }
}
