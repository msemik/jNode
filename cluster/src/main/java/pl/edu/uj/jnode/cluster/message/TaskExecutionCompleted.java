package pl.edu.uj.jnode.cluster.message;

import pl.edu.uj.jnode.cluster.Distributable;
import pl.edu.uj.jnode.cluster.Distributor;

import java.io.Serializable;
import java.util.Optional;

public class TaskExecutionCompleted implements Serializable, Distributable {
    private Object taskResultOrException;
    private String taskId;

    public TaskExecutionCompleted(Object taskResultOrException, String taskId) {
        this.taskResultOrException = taskResultOrException;
        this.taskId = taskId;
    }

    public Object getTaskResultOrException() {
        return taskResultOrException;
    }

    public String getTaskId() {
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
