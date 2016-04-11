package pl.edu.uj.jnode.engine.event;

import org.springframework.context.ApplicationEvent;

import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jnode.userlib.Callback;

/**
 * Created by alanhawrot on 08.04.2016.
 */
public class ExternalSubTaskReceivedEvent extends ApplicationEvent {
    private WorkerPoolTask task;
    private Callback callback;

    public ExternalSubTaskReceivedEvent(Object source, WorkerPoolTask task, Callback callback) {
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

    public String getSourceNodeId() {
        return task.getJar().getNodeId();
    }
}
