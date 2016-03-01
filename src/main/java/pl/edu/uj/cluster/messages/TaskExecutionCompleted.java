package pl.edu.uj.cluster.messages;

import pl.edu.uj.cluster.Distributor;

import java.io.Serializable;
import java.util.Optional;

public class TaskExecutionCompleted implements Serializable, Distributable {
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

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onTaskExecutionCompleted(taskId, taskResult);
    }
}
