package pl.edu.uj.cluster.task;

import pl.edu.uj.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jarpath.Jar;

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
    public Jar getJar() {
        return task.getJar();
    }

    @Override
    public long getTaskId() {
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

    @Override
    public boolean belongToJar(Jar jar) {
        return task.belongToJar(jar);
    }

    @Override
    public int getPriority() {
        return task.getPriority();
    }

    @Override
    public void incrementPriority() {
        task.incrementPriority();
    }
}
