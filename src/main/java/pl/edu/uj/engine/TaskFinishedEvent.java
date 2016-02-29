package pl.edu.uj.engine;

import org.springframework.context.ApplicationEvent;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;

/**
 * Created by alanhawrot on 28.02.2016.
 */
public class TaskFinishedEvent extends ApplicationEvent {
    private TaskFinalExecutionStatus status;
    private WorkerPoolTask task;
    private Object taskResult;
    private Throwable exception;

    public TaskFinishedEvent(Object source, TaskFinalExecutionStatus status, WorkerPoolTask task, Object taskResult) {
        super(source);
        this.status = status;
        this.task = task;
        this.taskResult = taskResult;
    }

    public TaskFinishedEvent(Object source, TaskFinalExecutionStatus status, WorkerPoolTask task, Throwable exception) {
        super(source);
        this.status = status;
        this.task = task;
        this.exception = exception;
    }

    public TaskFinalExecutionStatus getStatus() {
        return status;
    }

    public WorkerPoolTask getTask() {
        return task;
    }

    public Object getTaskResult() {
        return taskResult;
    }

    public Throwable getException() {
        return exception;
    }

    public enum TaskFinalExecutionStatus {
        SUCCESS, FAILURE
    }
}
