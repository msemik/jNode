package pl.edu.uj.jnode.engine.eventloop;

import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jnode.userlib.Task;

import java.io.Serializable;

/**
 * Created by alanhawrot on 14.11.2015.
 */
public class EventLoopResponse {
    private Type type;
    private WorkerPoolTask task;
    private Serializable taskResult;
    private Throwable exception;

    public EventLoopResponse(Type type) {
        this.type = type;
    }

    public EventLoopResponse(Type type, WorkerPoolTask task, Throwable exception) {
        this.type = type;
        this.exception = exception;
        this.task = task;
    }

    public EventLoopResponse(Type type, WorkerPoolTask task, Serializable taskResult) {
        this.type = type;
        this.taskResult = taskResult;
        this.task = task;
    }

    public Type getType() {
        return type;
    }

    public Serializable getTaskResult() {
        return taskResult;
    }

    public Throwable getException() {
        return exception;
    }

    public Task getTask() {
        return task;
    }

    public enum Type {
        SUCCESS, FAILURE, POISON
    }
}
