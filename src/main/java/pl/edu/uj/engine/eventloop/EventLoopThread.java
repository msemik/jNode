package pl.edu.uj.engine.eventloop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.ApplicationShutdownEvent;
import pl.edu.uj.engine.InvalidJarFileException;
import pl.edu.uj.engine.JarLauncher;
import pl.edu.uj.engine.UserApplicationException;
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

    private boolean shutdown = false;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private EventLoopThreadRegistry eventLoopThreadRegistry;

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
        eventLoopThreadRegistry.register(jarName, this);
        while (true) {
            if (shutdown)
                break;

            EventLoopResponse eventLoopResponse = eventLoopQueue.take();

            if (eventLoopResponse.getType() == EventLoopResponseType.POISON) {
                logger.info("Received poison, aborting.");
                break;
            }

            Task task = eventLoopResponse.getTask();
            Callback callback = callbackStorage.remove(task);

            if (eventLoopResponse.getType() == EventLoopResponseType.SUCCESS) {
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
                    break;
                }
            } else {
                Throwable exception = eventLoopResponse.getException();
                if (exception instanceof InvalidJarFileException) {
                    System.out.println(getJarName() + " is invalid jar file: " + exception.getMessage());
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
                    break;
                }
            }

            if (callbackStorage.isEmpty() && eventLoopQueue.isEmpty()) {
                logger.info("No more callbacks to execute, shutting down");
                break;
            }
        }

        eventLoopThreadRegistry.unregister(jarName);
        logger.info(getJarName() + " shutdown successfully");
    }

    public void startLoop(Path jarName) {
        this.jarName = jarName;
        this.jarLauncher = context.getBean(JarLauncher.class);
        jarLauncher.setPath(jarName);
        start();
    }

    public JarLauncher getJarLauncher() {
        return jarLauncher;
    }

    public void registerTask(WorkerPoolTask task, Callback callback) {
        callbackStorage.putIfAbsent(task, callback);
    }

    public void submitTaskResult(WorkerPoolTask task, Object taskResult) {
        eventLoopQueue.put(new EventLoopResponse(EventLoopResponseType.SUCCESS, task, taskResult));
    }

    public void submitTaskFailure(WorkerPoolTask task, Throwable ex) {
        eventLoopQueue.put(new EventLoopResponse(EventLoopResponseType.FAILURE, task, ex));
    }

    public Path getJarName() {
        return jarName;
    }

    public void shutDown() {
        shutdown = true;
        eventLoopQueue.poison();
        interrupt();
    }

    @Override
    public String toString() {
        return "EventLoopThread{" +
                "jarName=" + jarName +
                ", shutdown=" + shutdown +
                '}';
    }
}
