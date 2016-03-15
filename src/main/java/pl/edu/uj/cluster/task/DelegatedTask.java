package pl.edu.uj.cluster.task;

import pl.edu.uj.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jarpath.Jar;

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
