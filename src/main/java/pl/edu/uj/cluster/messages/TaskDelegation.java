package pl.edu.uj.cluster.messages;

import pl.edu.uj.cluster.ExternalTask;

import java.io.Serializable;

public class TaskDelegation implements Serializable {
    private ExternalTask task;

    public TaskDelegation(ExternalTask task) {
        this.task = task;
    }

    public ExternalTask getTask() {
        return task;
    }
}
