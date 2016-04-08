package pl.edu.uj.engine.events;

import org.springframework.context.ApplicationEvent;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;

/**
 * Created by alanhawrot on 28.02.2016.
 */
public class TaskFinishedEvent extends ApplicationEvent {
    private WorkerPoolTask task;
    private Object taskResultOrException;

    public TaskFinishedEvent(Object source, WorkerPoolTask task, Object taskResultOrException) {
        super(source);
        this.task = task;
        this.taskResultOrException = taskResultOrException;
    }

    public WorkerPoolTask getTask() {
        return task;
    }

    /**
     * If task finishes with success, this method will return the actual result.
     * If not, it returns the exception thrown during task execution.
     */
    public Object getTaskResultOrException() {
        return taskResultOrException;
    }

    public boolean isSuccess() {
        return !(taskResultOrException instanceof Throwable);
    }
}
