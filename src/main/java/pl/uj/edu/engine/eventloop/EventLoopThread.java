package pl.uj.edu.engine.eventloop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.uj.edu.ApplicationShutdownEvent;
import pl.uj.edu.engine.JarLauncher;
import pl.uj.edu.engine.workerpool.WorkerPoolTask;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

import javax.annotation.PostConstruct;
import java.nio.file.Path;

import static pl.uj.edu.engine.eventloop.EventLoopResponseType.FAILURE;
import static pl.uj.edu.engine.eventloop.EventLoopResponseType.SUCCESS;

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

    @EventListener
    public void onApplicationShutdown(ApplicationShutdownEvent e) {
        shutdown = true;
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

            if (eventLoopResponse.getType() == SUCCESS) {
                logger.info("Received task result, executing callback");
                Object taskResult = eventLoopResponse.getTaskResult();
                callback.onSuccess(taskResult);
            } else {
                logger.info("Received task exception, executing callback");
                Throwable exception = eventLoopResponse.getException();
                callback.onFailure(exception);
            }

            if(callbackStorage.isEmpty() && eventLoopQueue.isEmpty()) {
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
        eventLoopQueue.put(new EventLoopResponse(SUCCESS, task, taskResult));
    }

    public void submitTaskFailure(WorkerPoolTask task, Throwable ex) {
        eventLoopQueue.put(new EventLoopResponse(FAILURE, task, ex));
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
