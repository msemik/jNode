package pl.uj.edu.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationEventPublisher;
import org.xeustechnologies.jcl.JclUtils;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;
import pl.uj.edu.userlib.TaskReceiver;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configurable
public class DefaultTaskReceiver implements TaskReceiver {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TaskJarRegistry taskJarRegistry;

    public void doAsync(Object task, Object callback) {
        Class taskClass = task.getClass();
        URL resource = taskClass.getResource('/' + taskClass.getName().replace('.', '/') + ".class");

        if (resource == null) {
            return; // event, log
        }

        Pattern jarFilenamePattern = Pattern.compile("([\\w-]+\\.jar!)"); // need better pattern for filename
        Matcher matcher = jarFilenamePattern.matcher(resource.toString());

        if (!matcher.find()) {
            return; // event, log
        }

        String jarName = matcher.group().replace("!", "");

        Task taskToDo = JclUtils.cast(task, Task.class);
        Callback callbackToDo = JclUtils.cast(callback, Callback.class);

        taskJarRegistry.putIfAbsent(taskToDo, jarName);

        doAsync(taskToDo, callbackToDo);
    }

    public void doAsync(Task task, Callback callback) {
        eventPublisher.publishEvent(new NewTaskReceivedEvent(this, task, callback));
    }
}
