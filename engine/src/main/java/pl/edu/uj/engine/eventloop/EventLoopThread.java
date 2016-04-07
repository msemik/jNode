package pl.edu.uj.engine.eventloop;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.edu.uj.engine.*;
import pl.edu.uj.engine.event.*;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jarpath.Jar;
import pl.uj.edu.userlib.*;

import javax.annotation.PostConstruct;
import java.lang.reflect.*;
import java.util.*;

@Component
@Scope("prototype")
public class EventLoopThread extends Thread
{
    private Logger logger = LoggerFactory.getLogger(EventLoopThread.class);
    @Autowired
    private ApplicationContext context;
    @Autowired
    private EventLoopThreadPool eventLoopThreadPool;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired(required = false)
    private List<CallbackPreExecutionProcessor> callbackPreExecutionProcessors = new ArrayList<>();
    private CallbackStorage callbackStorage;
    private EventLoopQueue eventLoopQueue;

    private Jar jar;

    @PostConstruct
    public void init()
    {
        eventLoopQueue = context.getBean(EventLoopQueue.class);
        callbackStorage = context.getBean(CallbackStorage.class);
    }

    @Override
    public void run()
    {
        logger.info("Started to listen for tasks results");
        while(true)
        {
            eventPublisher.publishEvent(new JarJobsExecutionStartedEvent(this, getJar()));
            EventLoopResponse eventLoopResponse = eventLoopQueue.take();

            if(eventLoopResponse.getType() == EventLoopResponse.Type.POISON)
            {
                logger.info("Received poison, aborting.");
                break;
            }

            Task task = eventLoopResponse.getTask();
            Callback callback = callbackStorage.remove(task);
            for(CallbackPreExecutionProcessor processor : callbackPreExecutionProcessors)
            {
                callback = processor.process(jar, callback);
            }

            if(eventLoopResponse.getType() == EventLoopResponse.Type.SUCCESS)
            {
                logger.info("Received task result, executing callback");
                Object taskResult = eventLoopResponse.getTaskResult();
                try
                {
                    callback.onSuccess(taskResult);
                    logger.info("Callback execution finished successfully");
                }
                catch(RuntimeException ex)
                {
                    Throwable e = ex;
                    System.out.println("Error in " + getJar() + " while executing onSuccess, aborting jar jobs");

                    //Seeking user exception
                    if(e instanceof UndeclaredThrowableException)
                    {
                        e = e.getCause();
                    }
                    if(e instanceof InvocationTargetException)
                    {
                        e = e.getCause();
                    }

                    e.printStackTrace();
                    eventPublisher.publishEvent(new CancelJarJobsEvent(this, getJar()));
                    break;
                }
            }
            else
            {
                Throwable exception = eventLoopResponse.getException();
                if(exception instanceof InvalidJarFileException)
                {
                    System.out.println(getJar() + " is invalid jar file: " + exception.getMessage());
                    eventPublisher.publishEvent(new CancelJarJobsEvent(this, getJar()));
                    break;
                }

                //Seeking user exception
                if(exception instanceof UndeclaredThrowableException)
                {
                    exception = exception.getCause();
                }
                if(exception instanceof InvocationTargetException)
                {
                    exception = exception.getCause();
                }
                if(exception instanceof UserApplicationException)
                {
                    exception = exception.getCause();
                }
                logger.info("Received task exception(" + exception.getClass().getSimpleName() + ": " + exception.getMessage() + "), executing callback");
                try
                {
                    callback.onFailure(exception.getCause());
                }
                catch(RuntimeException ex)
                {
                    Throwable e = ex;
                    System.out.println("Error in " + getJar() + " while executing onFailure, aborting jar jobs");

                    //Seeking user exception
                    if(e instanceof UndeclaredThrowableException)
                    {
                        e = e.getCause();
                    }
                    if(e instanceof InvocationTargetException)
                    {
                        e = e.getCause();
                    }

                    e.printStackTrace();
                    eventPublisher.publishEvent(new CancelJarJobsEvent(this, getJar()));
                    break;
                }
            }

            if(eventLoopThreadPool.returnEventLoopThread(jar) == 0 && callbackStorage.isEmpty() && eventLoopQueue.isEmpty())
            {
                logger.info("No more callbacks to execute, shutting down");
                eventPublisher.publishEvent(new JarJobsCompletedEvent(this, getJar()));
                logger.info(getJar() + " loop shutdown successfully");
                break;
            }
        }
    }

    public Jar getJar()
    {
        return jar;
    }

    @Override
    public String toString()
    {
        return "EventLoopThread{" + jar + '}';
    }

    public void startLoop(Jar jar)
    {
        this.jar = jar;
        start();
    }

    public void registerTask(WorkerPoolTask task, Callback callback)
    {
        callbackStorage.putIfAbsent(task, callback);
    }

    public void submitTaskResult(WorkerPoolTask task, Object taskResult)
    {
        eventLoopQueue.put(new EventLoopResponse(EventLoopResponse.Type.SUCCESS, task, taskResult));
    }

    public void submitTaskFailure(WorkerPoolTask task, Throwable ex)
    {
        eventLoopQueue.put(new EventLoopResponse(EventLoopResponse.Type.FAILURE, task, ex));
    }

    public void shutDown()
    {
        interrupt();
        yield();
        if(isAlive())
        {
            eventLoopThreadPool.remove(jar);
            stop();
        }
        logger.info(getJar() + " shutdown() method execution finished");
    }
}
