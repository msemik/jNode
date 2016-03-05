package pl.edu.uj.engine.event;

import org.springframework.context.ApplicationEvent;
import pl.edu.uj.engine.event.CancellationEventOrigin;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;

/**
 * Created by alanhawrot on 01.03.2016.
 */
public class TaskCancelledEvent extends ApplicationEvent {
    private WorkerPoolTask task;
    private CancellationEventOrigin origin;

    public TaskCancelledEvent(Object source, WorkerPoolTask task, CancellationEventOrigin origin) {
        super(source);
        this.task = task;
        this.origin = origin;
    }

    public WorkerPoolTask getTask() {
        return task;
    }

    public CancellationEventOrigin getOrigin() {
        return origin;
    }
}
