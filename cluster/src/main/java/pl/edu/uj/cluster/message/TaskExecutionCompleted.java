package pl.edu.uj.cluster.message;

import pl.edu.uj.cluster.Distributable;
import pl.edu.uj.cluster.Distributor;

import java.io.Serializable;
import java.util.Optional;

public class TaskExecutionCompleted implements Serializable, Distributable {
    private Object taskResultOrException;
    private long taskId;

    public TaskExecutionCompleted(Object taskResultOrException, long taskId) {
        this.taskResultOrException = taskResultOrException;
        this.taskId = taskId;
    }

    public Object getTaskResultOrException() {
        return taskResultOrException;
    }

    public long getTaskId() {
        return taskId;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onTaskExecutionCompleted(taskId, taskResultOrException);
    }

    @Override
    public String toString() {
        return "TaskExecutionCompleted{" +
               "taskResultOrException=" + taskResultOrException +
               ", taskId=" + taskId +
               '}';
    }
}
