package pl.edu.uj.cluster.task;

import pl.edu.uj.cluster.node.Node;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;

import java.nio.file.Path;

/**
 * Created by alanhawrot on 29.02.2016.
 */
public class ExternalTask implements WorkerPoolTask {
    private WorkerPoolTask task;
    private String sourceNodeId;

    public ExternalTask(WorkerPoolTask task, String sourceNodeId) {
        this.task = task;
        this.sourceNodeId = sourceNodeId;
    }

    public WorkerPoolTask getTask() {
        return task;
    }

    public String getSourceNodeId() {
        return sourceNodeId;
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
        return true;
    }

    @Override
    public Object call() throws Exception {
        return task.call();
    }

    @Override
    public String toString() {
        return "ExternalTask{" +
                "task=" + task +
                ", sourceNodeId='" + sourceNodeId + '\'' +
                '}';
    }

    public boolean isOriginatedAt(Node selectedNode) {
        return sourceNodeId.equals(selectedNode.getNodeId());
    }
}
