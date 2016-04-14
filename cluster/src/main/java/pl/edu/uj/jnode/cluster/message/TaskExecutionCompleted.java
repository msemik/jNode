package pl.edu.uj.jnode.cluster.message;

import pl.edu.uj.jnode.cluster.Distributable;
import pl.edu.uj.jnode.cluster.Distributor;
import pl.edu.uj.jnode.cluster.task.SerializableTaskResultWrapper;

import java.io.Serializable;
import java.util.Optional;

public class TaskExecutionCompleted implements Serializable, Distributable {
    private SerializableTaskResultWrapper taskResultOrExceptionWrapper;
    private String taskId;

    public TaskExecutionCompleted(SerializableTaskResultWrapper taskResultOrExceptionWrapper, String taskId) {
        this.taskResultOrExceptionWrapper = taskResultOrExceptionWrapper;
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onTaskExecutionCompleted(taskId, taskResultOrExceptionWrapper);
    }

    @Override
    public String toString() {
        return "TaskExecutionCompleted{" +
               "taskResultOrExceptionWrapper=" + taskResultOrExceptionWrapper +
               ", taskId=" + taskId +
               '}';
    }
}
