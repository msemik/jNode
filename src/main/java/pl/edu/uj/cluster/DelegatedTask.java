package pl.edu.uj.cluster;

import pl.edu.uj.engine.workerpool.WorkerPoolTask;

/**
 * Created by alanhawrot on 28.02.2016.
 */
public class DelegatedTask extends ClusterTask {
    private String destinationNodeId;

    public DelegatedTask(WorkerPoolTask task) {
        super(task);
    }

    public String getDestinationNodeId() {
        return destinationNodeId;
    }

    public void setDestinationNodeId(String destinationNodeId) {
        this.destinationNodeId = destinationNodeId;
    }
}
