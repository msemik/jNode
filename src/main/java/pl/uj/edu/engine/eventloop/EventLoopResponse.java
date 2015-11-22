package pl.uj.edu.engine.eventloop;

import pl.uj.edu.engine.workerpool.WorkerPoolTask;
import pl.uj.edu.userlib.Task;

/**
 * Created by alanhawrot on 14.11.2015.
 */
public class EventLoopResponse {
    private EventLoopResponseType type;
    private WorkerPoolTask task;
    private Object taskResult;
    private Throwable exception;

    public EventLoopResponse(EventLoopResponseType type) {
        this.type = type;
    }

    public EventLoopResponse(EventLoopResponseType type, WorkerPoolTask task, Throwable exception) {
        this.type = type;
        this.exception = exception;
        this.task = task;
    }

    public EventLoopResponse(EventLoopResponseType type, WorkerPoolTask task, Object taskResult) {
        this.type = type;
        this.taskResult = taskResult;
        this.task = task;
    }

    public EventLoopResponseType getType() {
        return type;
    }


    public Object getTaskResult() {
        return taskResult;
    }


    public Throwable getException() {
        return exception;
    }


    public Task getTask() {
        return task;
    }
}
