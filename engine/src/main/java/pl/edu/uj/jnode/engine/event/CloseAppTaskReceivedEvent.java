package pl.edu.uj.jnode.engine.event;

import org.springframework.context.ApplicationEvent;
import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;

/**
 * Created by alanhawrot on 17.06.2016.
 */
public class CloseAppTaskReceivedEvent extends ApplicationEvent {
    private WorkerPoolTask task;

    public CloseAppTaskReceivedEvent(Object source, WorkerPoolTask task) {
        super(source);
        this.task = task;
    }

    public WorkerPoolTask getTask() {
        return task;
    }

    public String getSourceNodeId() {
        return task.getJar().getNodeId();
    }
}
