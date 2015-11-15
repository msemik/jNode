package pl.uj.edu.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationEventPublisher;
import org.xeustechnologies.jcl.JclUtils;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;
import pl.uj.edu.userlib.TaskReceiver;

@Configurable
public class DefaultTaskReceiver implements TaskReceiver {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void doAsync(Task task, Callback callback) {
        eventPublisher.publishEvent(new NewTaskCreatedEvent(this, task, callback));
    }

    public void doAsync(Object task, Object callback) {
        Task taskToDo = JclUtils.cast(task, Task.class);
        Callback callbackToDo = JclUtils.cast(callback, Callback.class);

        doAsync(taskToDo, callbackToDo);
    }
}
