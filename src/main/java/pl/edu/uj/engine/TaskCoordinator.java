package pl.edu.uj.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.engine.eventloop.EventLoopThread;
import pl.edu.uj.engine.eventloop.EventLoopThreadRegistry;
import pl.edu.uj.engine.workerpool.LaunchingMainClassWorkerPoolTask;
import pl.edu.uj.engine.workerpool.WorkerPool;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jarpath.JarDeletedEvent;
import pl.edu.uj.jarpath.JarPropertiesDeletedEvent;
import pl.edu.uj.jarpath.JarStateChangedEvent;
import pl.uj.edu.userlib.Callback;

import java.nio.file.Path;
import java.util.Optional;

import static pl.edu.uj.jarpath.JarExecutionState.NOT_STARTED;

@Component
public class TaskCoordinator {
    private Logger logger = LoggerFactory.getLogger(TaskCoordinator.class);

    @Autowired
    private WorkerPool workerPool;

    @Autowired
    private EventLoopThreadRegistry eventLoopThreadRegistry;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onJarStateChanged(JarStateChangedEvent event) {
        Path jarName = event.getPath();
        logger.info("Got jar " + jarName + " with properties " + event.getProperties());


        if (event.getProperties().getExecutionState() != NOT_STARTED)
            return;
        startJarJob(jarName);
    }

    private void startJarJob(Path jarName) {
        logger.info("Launching main class for jar " + jarName);

        EventLoopThread eventLoopThread = eventLoopThreadRegistry.createEventLoopThread(jarName);
        LaunchingMainClassWorkerPoolTask task = new LaunchingMainClassWorkerPoolTask(eventLoopThread);
        EmptyCallback callback = new EmptyCallback();
        eventLoopThread.registerTask(task, callback);
        workerPool.submitTask(task);
    }

    @EventListener
    public void onJarDeleted(JarDeletedEvent event) {
        eventPublisher.publishEvent(new CancelJarJobsEvent(this, event.getJarPath()));
    }

    @EventListener
    public void onJarPropertiesDeleted(JarPropertiesDeletedEvent event) {
        Path jarFileName = event.getJarFileName();
        if (eventLoopThreadRegistry.forJarName(jarFileName).isPresent())
            eventPublisher.publishEvent(new CancelJarJobsEvent(this, jarFileName));
        else
            startJarJob(jarFileName);
    }

    @EventListener
    public void onNewTaskReceived(NewTaskReceivedEvent event) {
        WorkerPoolTask task = event.getTask();
        Callback callback = event.getCallback();

        logger.info("Submitting newly received task to pool and saving callback in EventLoopThread " + task);

        Optional<EventLoopThread> eventLoopThread = eventLoopThreadRegistry.forJarName(task.getJarName());
        if (!eventLoopThread.isPresent()) {
            logger.error("Event loop thread is missing when received task: " + task + " " + eventLoopThreadRegistry);
            return;
        }
        eventLoopThread.get().registerTask(task, callback);
        workerPool.submitTask(task);
    }
}
