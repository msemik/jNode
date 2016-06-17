package pl.edu.uj.jnode.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import pl.edu.uj.jnode.engine.event.CloseAppTaskReceivedEvent;
import pl.edu.uj.jnode.engine.event.ExternalSubTaskReceivedEvent;
import pl.edu.uj.jnode.engine.event.TaskReceivedEvent;
import pl.edu.uj.jnode.engine.workerpool.DefaultWorkerPoolTask;
import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jnode.jarpath.JarFactory;
import pl.edu.uj.jnode.jarpath.JarPathServices;
import pl.edu.uj.jnode.userlib.Callback;
import pl.edu.uj.jnode.userlib.Task;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configurable
public class DefaultTaskReceiver {
    private Logger logger = LoggerFactory.getLogger(DefaultTaskReceiver.class);
    @Autowired
    private ApplicationContext applicationContext;
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
    @Autowired(required = false)
    private BeanProvider beanProvider;

    public void doAsync(Object task, Object callback) {
        Path pathToJar = discoverPathToJarUsingUserClass(callback.getClass());
        logger.info("Discovered jar filename: " + pathToJar);

        Task taskToDo = Task.class.cast(task);
        Callback callbackToDo = Callback.class.cast(callback);

        WorkerPoolTask workerPoolTask = new DefaultWorkerPoolTask(taskToDo, jarFactory.getFor(pathToJar), nodeIdFactory.getCurrentNodeId());
        if (workerPoolTask.getJar().isExternal()) {
            eventPublisher.publishEvent(new TaskReceivedEvent(this, workerPoolTask, callbackToDo));
        } else {
            eventPublisher.publishEvent(new ExternalSubTaskReceivedEvent(this, workerPoolTask, callbackToDo));
        }
    }

    public void closeAppAsync(Object preCloseAppTask) {
        Path pathToJar = discoverPathToJarUsingUserClass(preCloseAppTask.getClass());
        logger.info("Discovered jar filename: " + pathToJar);

        Task taskToDo = Task.class.cast(preCloseAppTask);

        WorkerPoolTask workerPoolTask = new DefaultWorkerPoolTask(taskToDo, jarFactory.getFor(pathToJar), nodeIdFactory.getCurrentNodeId());
        eventPublisher.publishEvent(new CloseAppTaskReceivedEvent(this, workerPoolTask));
    }

    private Path discoverPathToJarUsingUserClass(Class<?> cls) {
        logger.info("Class name to discover pathToJar: " + cls.getName());
        URL resource = cls.getResource('/' + cls.getName().replace('.', '/') + ".class");

        if (resource == null) {
            logger.error("null resource occurred");
            return Paths.get("");
        }

        String[] resourcePartition = resource.toString().split("!");
        if (resourcePartition.length != 2) {
            throw new IllegalStateException("Unexpected jar resource format occurred:" + resource);
        }

        try {
            String decodedPath = URLDecoder.decode(resourcePartition[0], "utf-8");
            if (decodedPath.startsWith("jar:file:")) {
                decodedPath = decodedPath.substring("jar:file:".length());
            }
            return jarPathServices.getPathSinceJarPath(Paths.get(decodedPath).normalize());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Decoded path has unexpected format " + resourcePartition[0], e);
        }
    }

    public long getAvailableWorkers() {
        return computationResourcesProvider.getAvailableWorkers();
    }

    public long getTotalWorkers() {
        return computationResourcesProvider.getTotalWorkers();
    }

    public Object getBean(Class<?> cls) {
        return beanProvider.getBean(cls, discoverPathToJarUsingUserClass(cls)).orElse(null);
    }
}
