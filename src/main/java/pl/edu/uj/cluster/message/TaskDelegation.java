package pl.edu.uj.cluster.message;

import pl.edu.uj.cluster.Distributable;
import pl.edu.uj.cluster.Distributor;
import pl.edu.uj.cluster.task.ExternalTask;

import java.io.Serializable;
import java.util.Optional;

public class TaskDelegation implements Serializable, Distributable {
    private ExternalTask task;

    public TaskDelegation(ExternalTask task) {
        this.task = task;
    }

    public ExternalTask getTask() {
        return task;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onTaskDelegation(task);
    }

    @Override
    public String toString() {
        return "TaskDelegation{" + task + '}';
    }
}
