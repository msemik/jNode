package pl.edu.uj.cluster.messages;

import java.io.Serializable;

public class Sry implements Serializable {
    private long taskId;

    public Sry(long taskId) {
        this.taskId = taskId;
    }

    public long getTaskId() {
        return taskId;
    }
}
