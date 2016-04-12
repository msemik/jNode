package pl.edu.uj.jnode.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationEventPublisher;

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
    private JarPathServices jarPathServices;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private JarFactory jarFactory;
    @Autowired
    private NodeIdFactory nodeIdFactory;

    public void doAsync(Object task, Object callback) {
        Class callbackClass = callback.getClass();
        logger.info("Callback class name: " + callbackClass.getName());
        URL resource = callbackClass.getResource('/' + callbackClass.getName().replace('.', '/') + ".class");

        if (resource == null) {
            logger.error("null resource occurred");
            return; // event, log
        }

        String[] resourcePartition = resource.toString().split("!");
        if (resourcePartition == null || resourcePartition.length != 2) {
            throw new IllegalStateException("Unexpected jar resource format occurred:" + resource);
        }

        Path pathToJar;
        try {
            String decodedPath = URLDecoder.decode(resourcePartition[0], "utf-8");
            if (decodedPath.startsWith("jar:file:")) {
                decodedPath = decodedPath.substring("jar:file:".length());
            }
            pathToJar = jarPathServices.getPathSinceJarPath(Paths.get(decodedPath).normalize());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Decoded path has unexpected format " + resourcePartition[0], e);
        }
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
}
