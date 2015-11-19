package pl.uj.edu.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationEventPublisher;
import org.xeustechnologies.jcl.JclUtils;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;
import pl.uj.edu.userlib.TaskReceiver;

import java.net.URL;
import java.nio.file.Paths;

@Configurable
public class DefaultTaskReceiver implements TaskReceiver {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TaskJarRegistry taskJarRegistry;

    private Logger logger = LoggerFactory.getLogger(DefaultTaskReceiver.class);

    public void doAsync(Object task, Object callback) {
        Class taskClass = task.getClass();
        URL resource = taskClass.getResource('/' + taskClass.getName().replace('.', '/') + ".class");

        if (resource == null) {
            logger.info("null resource occurred");
            return; // event, log
        }

        String[] resourcePartition = resource.toString().split("!");
        if(resourcePartition == null || resourcePartition.length != 2)
            throw new IllegalStateException("Unexpected jar resource format occurred:" + resource);

        String jarName = Paths.get(resourcePartition[0]).getFileName().toString();
        logger.info("Discovered jar filename from task:" + jarName);
        Task taskToDo = JclUtils.cast(task, Task.class);
        Callback callbackToDo = JclUtils.cast(callback, Callback.class);

        taskJarRegistry.putIfAbsent(taskToDo, jarName);

        doAsync(taskToDo, callbackToDo);
    }

    public void doAsync(Task task, Callback callback) {
        eventPublisher.publishEvent(new NewTaskReceivedEvent(this, task, callback));
    }
}
