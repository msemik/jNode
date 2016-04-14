package pl.edu.uj.jnode.engine.event;

import org.springframework.context.ApplicationEvent;
import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;

import java.io.Serializable;

/**
 * Created by alanhawrot on 28.02.2016.
 */
public class TaskFinishedEvent extends ApplicationEvent {
    private WorkerPoolTask task;
    private Serializable taskResultOrException;

    public TaskFinishedEvent(Object source, WorkerPoolTask task, Serializable taskResultOrException) {
        super(source);
        this.task = task;
        this.taskResultOrException = taskResultOrException;
    }

    public WorkerPoolTask getTask() {
        return task;
    }

    /**
     * If task finishes with success, this method will return the actual result. If not, it returns
     * the exception thrown during task execution.
     */
    public Serializable getTaskResultOrException() {
        return taskResultOrException;
    }

    public boolean isSuccess() {
        return !(taskResultOrException instanceof Throwable);
    }
}
