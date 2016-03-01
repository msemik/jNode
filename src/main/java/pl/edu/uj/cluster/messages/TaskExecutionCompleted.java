package pl.edu.uj.cluster.messages;

import java.io.Serializable;

public class TaskExecutionCompleted implements Serializable {
    private Object taskResult;
    private long taskId;

    public TaskExecutionCompleted(Object taskResult, long taskId) {
        this.taskResult = taskResult;
        this.taskId = taskId;
    }

    public Object getTaskResult() {
        return taskResult;
    }

    public long getTaskId() {
        return taskId;
    }
}
