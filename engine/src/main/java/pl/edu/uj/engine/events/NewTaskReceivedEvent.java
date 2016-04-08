package pl.edu.uj.engine.events;

import org.springframework.context.ApplicationEvent;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.userlib.Callback;

public class NewTaskReceivedEvent extends ApplicationEvent {
    private WorkerPoolTask task;
    private Callback callback;

    public NewTaskReceivedEvent(Object source, WorkerPoolTask task, Callback callback) {
        super(source);
        this.task = task;
        this.callback = callback;
    }

    public WorkerPoolTask getTask() {
        return task;
    }

    public Callback getCallback() {
        return callback;
    }
}
