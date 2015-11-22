package pl.edu.uj.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationEventPublisher;
import org.xeustechnologies.jcl.JclUtils;
import pl.edu.uj.engine.workerpool.UserDoAsyncWorkerPoolTask;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;
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
    private ApplicationEventPublisher eventPublisher;

    private Logger logger = LoggerFactory.getLogger(DefaultTaskReceiver.class);

    public void doAsync(Object task, Object callback) {
        Class taskClass = task.getClass();
        URL resource = taskClass.getResource('/' + taskClass.getName().replace('.', '/') + ".class");

        if (resource == null) {
            logger.error("null resource occurred");
            return; // event, log
        }

        String[] resourcePartition = resource.toString().split("!");
        if (resourcePartition == null || resourcePartition.length != 2)
            throw new IllegalStateException("Unexpected jar resource format occurred:" + resource);

        Path jarName = null;
        try {
            jarName = Paths.get(URLDecoder.decode(resourcePartition[0], "utf-8")).getFileName();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Decoded path has unexpected format " + resourcePartition[0], e);
        }
        logger.info("Discovered jar filename from task:" + jarName);
        Task taskToDo = JclUtils.cast(task, Task.class);
        Callback callbackToDo = JclUtils.cast(callback, Callback.class);

        WorkerPoolTask workerPoolTask = new UserDoAsyncWorkerPoolTask(taskToDo, jarName);
        NewTaskReceivedEvent event = new NewTaskReceivedEvent(this, workerPoolTask, callbackToDo);
        eventPublisher.publishEvent(event);
    }

}
