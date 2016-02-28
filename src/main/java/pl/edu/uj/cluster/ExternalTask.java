package pl.edu.uj.cluster;

import pl.edu.uj.engine.workerpool.WorkerPoolTask;

/**
 * Created by alanhawrot on 28.02.2016.
 */
public class ExternalTask extends ClusterTask {
    private String sourceNodeId;

    public ExternalTask(WorkerPoolTask task) {
        super(task);
    }

    public String getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(String sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
    }
}
