package pl.edu.uj.jnode.engine.eventloop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.engine.CallbackPreExecutionProcessor;
import pl.edu.uj.jnode.engine.InvalidJarFileException;
import pl.edu.uj.jnode.engine.UserApplicationException;
import pl.edu.uj.jnode.engine.event.CancelJarJobsEvent;
import pl.edu.uj.jnode.engine.event.JarJobsCompletedEvent;
import pl.edu.uj.jnode.engine.event.JarJobsExecutionStartedEvent;
import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.userlib.Callback;
import pl.edu.uj.jnode.userlib.Task;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
    @Autowired(required = false)
    private List<CallbackPreExecutionProcessor> callbackPreExecutionProcessors = new ArrayList<>();
    private CallbackStorage callbackStorage;
    private EventLoopQueue eventLoopQueue;
    private Jar jar;
    private AtomicBoolean isClosing = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        eventLoopQueue = context.getBean(EventLoopQueue.class);
        callbackStorage = context.getBean(CallbackStorage.class);
    }

    @Override
    public void run() {
        logger.info("Started to listen for tasks results");
        eventPublisher.publishEvent(new JarJobsExecutionStartedEvent(this, getJar()));

        while (!isInterrupted()) {
            EventLoopResponse eventLoopResponse = eventLoopQueue.take();

            if (isInterrupted()) {
                break;
            }

            if (eventLoopResponse.getType() == EventLoopResponse.Type.POISON) {
                logger.info("Received poison, aborting.");
                break;
            }

            Task task = eventLoopResponse.getTask();
            Callback callback = callbackStorage.remove(task);
            for (CallbackPreExecutionProcessor processor : callbackPreExecutionProcessors) {
                callback = processor.process(jar, callback);
            }

            if (eventLoopResponse.getType() == EventLoopResponse.Type.SUCCESS) {
                logger.info("Received task result, executing callback");
                Serializable taskResult = eventLoopResponse.getTaskResult();
                try {
                    callback.onSuccess(taskResult);
                    logger.info("Callback execution finished successfully");
                } catch (RuntimeException ex) {
                    Throwable e = ex;
                    System.out.println("Error in " + getJar() + " while executing onSuccess, aborting jar jobs");

                    //Seeking user exception
                    if (e instanceof UndeclaredThrowableException) {
                        e = e.getCause();
                    }
                    if (e instanceof InvocationTargetException) {
                        e = e.getCause();
                    }

                    e.printStackTrace();
                    eventPublisher.publishEvent(new CancelJarJobsEvent(this, getJar()));
                    break;
                }
            } else {
                Throwable exception = eventLoopResponse.getException();
                if (exception instanceof InvalidJarFileException) {
                    System.out.println(getJar() + " is invalid jar file: " + exception.getMessage());
                    eventPublisher.publishEvent(new CancelJarJobsEvent(this, getJar()));
                    break;
                }

                //Seeking user exception
                if (exception instanceof UndeclaredThrowableException) {
                    exception = exception.getCause();
                }
                if (exception instanceof InvocationTargetException) {
                    exception = exception.getCause();
                }
                if (exception instanceof UserApplicationException) {
                    exception = exception.getCause();
                }
                logger.info("Received task exception(" + exception.getClass().getSimpleName() + ": " + exception.getMessage() + "), executing callback");
                try {
                    callback.onFailure(exception.getCause());
                } catch (RuntimeException ex) {
                    Throwable e = ex;
                    System.out.println("Error in " + getJar() + " while executing onFailure, aborting jar jobs");

                    //Seeking user exception
                    if (e instanceof UndeclaredThrowableException) {
                        e = e.getCause();
                    }
                    if (e instanceof InvocationTargetException) {
                        e = e.getCause();
                    }

                    e.printStackTrace();
                    eventPublisher.publishEvent(new CancelJarJobsEvent(this, getJar()));
                    break;
                }
            }

            if (callbackStorage.isEmpty() && eventLoopQueue.isEmpty()) {
                closeLoop();
                break;
            }
        }
    }

    public void startLoop(Jar jar) {
        this.jar = jar;
        start();
    }

    private void closeLoop() {
        logger.info("No more callbacks to execute, shutting down");
        eventPublisher.publishEvent(new JarJobsCompletedEvent(this, getJar()));
        eventLoopThreadRegistry.remove(jar);
        logger.info(getJar() + " loop shutdown successfully");
    }

    public boolean closeApp() {
        boolean isClosingValue = isClosing.getAndSet(true);
        if (!isClosingValue) {
            logger.warn("Closing application on demand");
            eventPublisher.publishEvent(new CancelJarJobsEvent(this, getJar())); // remove elt from registry and other
            shutDown(); // because closeApp method is executed from other thread in worker pool
        }
        return isClosingValue;
    }

    public Jar getJar() {
        return jar;
    }

    public void registerTask(WorkerPoolTask task, Callback callback) {
        callbackStorage.putIfAbsent(task, callback);
    }

    public void submitTaskResult(WorkerPoolTask task, Serializable taskResult) {
        eventLoopQueue.put(new EventLoopResponse(EventLoopResponse.Type.SUCCESS, task, taskResult));
    }

    public void submitTaskFailure(WorkerPoolTask task, Throwable ex) {
        eventLoopQueue.put(new EventLoopResponse(EventLoopResponse.Type.FAILURE, task, ex));
    }

    public void shutDown() {
        interrupt();
        yield();
        logger.info(getJar() + " shutdown() method execution finished");
    }

    @Override
    public String toString() {
        return "EventLoopThread{" + jar + '}';
    }
}
