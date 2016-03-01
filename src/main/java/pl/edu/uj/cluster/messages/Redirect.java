package pl.edu.uj.cluster.messages;

import java.io.Serializable;

public class Redirect implements Serializable {
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
}
