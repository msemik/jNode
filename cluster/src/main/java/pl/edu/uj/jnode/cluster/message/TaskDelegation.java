package pl.edu.uj.jnode.cluster.message;

import pl.edu.uj.jnode.cluster.Distributable;
import pl.edu.uj.jnode.cluster.Distributor;
import pl.edu.uj.jnode.cluster.task.ExternalTask;

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
