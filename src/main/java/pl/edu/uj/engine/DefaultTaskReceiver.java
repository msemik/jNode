package pl.edu.uj.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationEventPublisher;
import pl.edu.uj.engine.event.NewTaskReceivedEvent;
import pl.edu.uj.engine.workerpool.UserDoAsyncWorkerPoolTask;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jarpath.JarFactory;
import pl.edu.uj.jarpath.JarPathServices;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configurable
public class DefaultTaskReceiver {
    @Autowired
    private JarPathServices jarPathServices;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private JarFactory jarFactory;


    private Logger logger = LoggerFactory.getLogger(DefaultTaskReceiver.class);

    public void doAsync(Object task, Object callback) {
        Class callbackClass = callback.getClass();
        URL resource = callbackClass.getResource('/' + callbackClass.getName().replace('.', '/') + ".class");

        if (resource == null) {
            logger.error("null resource occurred");
            return; // event, log
        }

        String[] resourcePartition = resource.toString().split("!");
        if (resourcePartition == null || resourcePartition.length != 2)
            throw new IllegalStateException("Unexpected jar resource format occurred:" + resource);

        Path pathToJar;
        try {
            String decodedPath = URLDecoder.decode(resourcePartition[0], "utf-8");
            if (decodedPath.startsWith("jar:file:"))
                decodedPath = decodedPath.substring("jar:file:".length());
            pathToJar = jarPathServices.getPathSinceJarPath(Paths.get(decodedPath).normalize());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Decoded path has unexpected format " + resourcePartition[0], e);
        }
        logger.info("Discovered jar filename from task:" + pathToJar);
        Task taskToDo = Task.class.cast(task);
        Callback callbackToDo = Callback.class.cast(callback);

        WorkerPoolTask workerPoolTask = new UserDoAsyncWorkerPoolTask(taskToDo, jarFactory.getFor(pathToJar));
        NewTaskReceivedEvent event = new NewTaskReceivedEvent(this, workerPoolTask, callbackToDo);
        eventPublisher.publishEvent(event);
    }

}
