package pl.edu.uj.engine.eventloop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.edu.uj.engine.*;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Path;

@Component
@Scope("prototype")
public class EventLoopThread extends Thread {
    private Logger logger = LoggerFactory.getLogger(EventLoopThread.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private EventLoopThreadRegistry eventLoopThreadRegistry;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private CallbackStorage callbackStorage;
    private EventLoopQueue eventLoopQueue;
    private Path jarName;
    private JarLauncher jarLauncher;

    @PostConstruct
    public void init() {
        eventLoopQueue = context.getBean(EventLoopQueue.class);
        callbackStorage = context.getBean(CallbackStorage.class);
    }

    @Override
    public void run() {
        logger.info("Started to listen for tasks results");
        while (true) {
            eventPublisher.publishEvent(new JarJobsExecutionStartedEvent(this, getJarName()));
            EventLoopResponse eventLoopResponse = eventLoopQueue.take();

            if (eventLoopResponse.getType() == EventLoopResponse.Type.POISON) {
                logger.info("Received poison, aborting.");
                break;
            }

            Task task = eventLoopResponse.getTask();
            Callback callback = callbackStorage.remove(task);

            if (eventLoopResponse.getType() == EventLoopResponse.Type.SUCCESS) {
                logger.info("Received task result, executing callback");
                Object taskResult = eventLoopResponse.getTaskResult();
                try {
                    callback.onSuccess(taskResult);
                    logger.info("Callback execution finished successfully");
                } catch (RuntimeException ex) {
                    Throwable e = ex;
                    System.out.println("Error in " + getJarName() + " while executing onSuccess, aborting jar jobs");

                    //Seeking user exception
                    if (e instanceof UndeclaredThrowableException)
                        e = e.getCause();
                    if (e instanceof InvocationTargetException)
                        e = e.getCause();

                    e.printStackTrace();
                    eventPublisher.publishEvent(new CancelJarJobsEvent(this, getJarName()));
                    break;
                }
            } else {
                Throwable exception = eventLoopResponse.getException();
                if (exception instanceof InvalidJarFileException) {
                    System.out.println(getJarName() + " is invalid jar file: " + exception.getMessage());
                    eventPublisher.publishEvent(new CancelJarJobsEvent(this, getJarName()));
                    break;
                }

                //Seeking user exception
                if (exception instanceof UndeclaredThrowableException)
                    exception = exception.getCause();
                if (exception instanceof InvocationTargetException)
                    exception = exception.getCause();
                if (exception instanceof UserApplicationException)
                    exception = exception.getCause();
                logger.info("Received task exception(" + exception.getClass().getSimpleName() + ": " + exception.getMessage() + "), executing callback");
                try {
                    callback.onFailure(exception.getCause());
                } catch (RuntimeException ex) {
                    Throwable e = ex;
                    System.out.println("Error in " + getJarName() + " while executing onFailure, aborting jar jobs");

                    //Seeking user exception
                    if (e instanceof UndeclaredThrowableException)
                        e = e.getCause();
                    if (e instanceof InvocationTargetException)
                        e = e.getCause();

                    e.printStackTrace();
                    eventPublisher.publishEvent(new CancelJarJobsEvent(this, getJarName()));
                    break;
                }
            }

            if (callbackStorage.isEmpty() && eventLoopQueue.isEmpty()) {
                logger.info("No more callbacks to execute, shutting down");
                eventPublisher.publishEvent(new JarJobsCompletedEvent(this, getJarName()));
                break;
            }
        }

        eventLoopThreadRegistry.unregister(jarName);
        logger.info(getJarName() + " loop shutdown successfully");
    }

    public Path getJarName() {
        return jarName;
    }

    @Override
    public String toString() {
        return "EventLoopThread{" + jarName + '}';
    }

    public void startLoop(Path jarName) {
        this.jarName = jarName;
        this.jarLauncher = context.getBean(JarLauncher.class);
        jarLauncher.setPath(jarName);
        start();
        eventLoopThreadRegistry.register(jarName, this);
    }

    public JarLauncher getJarLauncher() {
        return jarLauncher;
    }

    public void registerTask(WorkerPoolTask task, Callback callback) {
        callbackStorage.putIfAbsent(task, callback);
    }

    public void submitTaskResult(WorkerPoolTask task, Object taskResult) {
        eventLoopQueue.put(new EventLoopResponse(EventLoopResponse.Type.SUCCESS, task, taskResult));
    }

    public void submitTaskFailure(WorkerPoolTask task, Throwable ex) {
        eventLoopQueue.put(new EventLoopResponse(EventLoopResponse.Type.FAILURE, task, ex));
    }

    public void shutDown() {
        interrupt();
        yield();
        if (isAlive()) {
            eventLoopThreadRegistry.unregister(jarName);
            stop();
        }
        logger.info(getJarName() + " shutdown() method execution finished");
    }
}
