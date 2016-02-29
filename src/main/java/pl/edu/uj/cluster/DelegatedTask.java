package pl.edu.uj.cluster;

import pl.edu.uj.engine.workerpool.WorkerPoolTask;

import java.nio.file.Path;

/**
 * Created by alanhawrot on 29.02.2016.
 */
public class DelegatedTask implements WorkerPoolTask {
    private WorkerPoolTask task;
    private String destinationNodeId;

    public DelegatedTask(WorkerPoolTask task, String destinationNodeId) {
        this.task = task;
        this.destinationNodeId = destinationNodeId;
    }

    public WorkerPoolTask getTask() {
        return task;
    }

    public String getDestinationNodeId() {
        return destinationNodeId;
    }

    public void setDestinationNodeId(String destinationNodeId) {
        this.destinationNodeId = destinationNodeId;
    }

    @Override
    public Path getJarName() {
        return task.getJarName();
    }

    @Override
    public int getTaskId() {
        return task.getTaskId();
    }

    @Override
    public boolean isExternal() {
        return false;
    }

    @Override
    public Object call() throws Exception {
        return task.call();
    }
}
