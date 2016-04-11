package pl.edu.uj.jnode.cluster.message;

import pl.edu.uj.jnode.cluster.Distributable;
import pl.edu.uj.jnode.cluster.Distributor;

import java.io.Serializable;
import java.util.Optional;

public class Redirect implements Serializable, Distributable {
    private String destinationNodeId;
    private long taskId;

    public Redirect(String destinationNodeId, long taskId) {
        this.destinationNodeId = destinationNodeId;
        this.taskId = taskId;
    }

    public String getDestinationNodeId() {
        return destinationNodeId;
    }

    public long getTaskId() {
        return taskId;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onRedirect(sourceNodeId, this.destinationNodeId, taskId);
    }

    @Override
    public String toString() {
        return "Redirect{" +
                "destinationNodeId='" + destinationNodeId + '\'' +
                ", taskId=" + taskId +
                '}';
    }
}
