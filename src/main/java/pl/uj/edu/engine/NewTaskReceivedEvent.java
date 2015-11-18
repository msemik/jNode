package pl.uj.edu.engine;

import org.springframework.context.ApplicationEvent;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

public class NewTaskReceivedEvent extends ApplicationEvent {
    private Task task;
    private Callback callback;

    public NewTaskReceivedEvent(Object source, Task task, Callback callback) {
        super(source);
        this.task = task;
        this.callback = callback;
    }

    public Task getTask() {
        return task;
    }

    public Callback getCallback() {
        return callback;
    }
}
