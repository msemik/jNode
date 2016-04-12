package pl.edu.uj.jnode.cluster.message;

import pl.edu.uj.jnode.cluster.Distributable;
import pl.edu.uj.jnode.cluster.Distributor;

import java.io.Serializable;
import java.util.Optional;

public class Sry implements Serializable, Distributable {
    private String taskId;

    public Sry(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onSry(sourceNodeId, taskId);
    }

    @Override
    public String toString() {
        return "Sry{" + "taskId=" + taskId + '}';
    }
}
