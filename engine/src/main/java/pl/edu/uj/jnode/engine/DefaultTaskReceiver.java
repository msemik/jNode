package pl.edu.uj.jnode.engine;

import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.ApplicationEventPublisher;
import pl.edu.uj.jnode.engine.event.*;
import pl.edu.uj.jnode.engine.workerpool.*;
import pl.edu.uj.jnode.jarpath.*;
import pl.edu.uj.jnode.userlib.*;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.file.*;

@Configurable
public class DefaultTaskReceiver
{
    private Logger logger = LoggerFactory.getLogger(DefaultTaskReceiver.class);
    @Autowired
    private JarPathServices jarPathServices;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private JarFactory jarFactory;
    @Autowired
    private NodeIdFactory nodeIdFactory;
    @Autowired
    private ComputationResourcesProvider computationResourcesProvider;
    @Autowired
    private BeanProvider beanProvider;

    public void doAsync(Object task, Object callback)
    {
        Path pathToJar = discoverPathToJarUsingUserClass(callback.getClass());

        logger.info("Discovered jar filename: " + pathToJar);
        Task taskToDo = Task.class.cast(task);
        Callback callbackToDo = Callback.class.cast(callback);

        WorkerPoolTask workerPoolTask = new DefaultWorkerPoolTask(taskToDo, jarFactory.getFor(pathToJar), nodeIdFactory.getCurrentNodeId());
        if(workerPoolTask.getJar().isExternal())
        {
            eventPublisher.publishEvent(new TaskReceivedEvent(this, workerPoolTask, callbackToDo));
        }
        else
        {
            eventPublisher.publishEvent(new ExternalSubTaskReceivedEvent(this, workerPoolTask, callbackToDo));
        }
    }

    private Path discoverPathToJarUsingUserClass(Class<?> cls)
    {
        logger.info("Class name to discover pathToJar: " + cls.getName());
        URL resource = cls.getResource('/' + cls.getName().replace('.', '/') + ".class");

        if(resource == null)
        {
            logger.error("null resource occurred");
            return Paths.get("");
        }

        String[] resourcePartition = resource.toString().split("!");
        if(resourcePartition == null || resourcePartition.length != 2)
        {
            throw new IllegalStateException("Unexpected jar resource format occurred:" + resource);
        }

        try
        {
            String decodedPath = URLDecoder.decode(resourcePartition[0], "utf-8");
            if(decodedPath.startsWith("jar:file:"))
            {
                decodedPath = decodedPath.substring("jar:file:".length());
            }
            return jarPathServices.getPathSinceJarPath(Paths.get(decodedPath).normalize());
        }
        catch(UnsupportedEncodingException e)
        {
            throw new IllegalStateException("Decoded path has unexpected format " + resourcePartition[0], e);
        }
    }

    public long getAvailableWorkers()
    {
        return computationResourcesProvider.getAvailableWorkers();
    }

    public long getTotalWorkers()
    {
        return computationResourcesProvider.getTotalWorkers();
    }

    public Object getBean(Class<?> cls)
    {
        return beanProvider.getBean(cls, discoverPathToJarUsingUserClass(cls)).orElse(null);
    }
}
