package pl.edu.uj.jnode.cluster.task;

import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;

/**
 * Created by alanhawrot on 29.02.2016.
 */
public class DelegatedTask extends WorkerPoolTaskDecorator {
    private String destinationNodeId;

    public DelegatedTask(WorkerPoolTask task, String destinationNodeId) {
        super(task);
        this.destinationNodeId = destinationNodeId;
    }

    public String getDestinationNodeId() {
        return destinationNodeId;
    }

    public void setDestinationNodeId(String destinationNodeId) {
        this.destinationNodeId = destinationNodeId;
    }
}
