package pl.uj.edu.engine;

import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.TaskResult;

/**
 * Created by alanhawrot on 14.11.2015.
 */
public class EventLoopRespond {
    private EventLoopRespondType type;
    private Callback callback;
    private TaskResult taskResult;

    public EventLoopRespond(EventLoopRespondType type) {
        this.type = type;
    }

    public EventLoopRespond(EventLoopRespondType type, Callback callback, TaskResult taskResult) {
        this.type = type;
        this.callback = callback;
        this.taskResult = taskResult;
    }

    public EventLoopRespondType getType() {
        return type;
    }

    public void setType(EventLoopRespondType type) {
        this.type = type;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public TaskResult getTaskResult() {
        return taskResult;
    }

    public void setTaskResult(TaskResult taskResult) {
        this.taskResult = taskResult;
    }
}
