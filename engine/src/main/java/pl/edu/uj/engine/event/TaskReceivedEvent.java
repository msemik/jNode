package pl.edu.uj.engine.event;

import org.springframework.context.ApplicationEvent;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.userlib.Callback;

public class TaskReceivedEvent extends ApplicationEvent {
    private WorkerPoolTask task;
    private Callback callback;

    public TaskReceivedEvent(Object source, WorkerPoolTask task, Callback callback) {
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
