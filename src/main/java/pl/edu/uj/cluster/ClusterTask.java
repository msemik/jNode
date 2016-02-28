package pl.edu.uj.cluster;

import pl.edu.uj.engine.workerpool.WorkerPoolTask;

import java.nio.file.Path;

/**
 * Created by alanhawrot on 28.02.2016.
 */
public abstract class ClusterTask implements WorkerPoolTask {
    private final WorkerPoolTask task;

    protected ClusterTask(WorkerPoolTask task) {
        this.task = task;
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
    public Object call() throws Exception {
        return task.call();
    }
}
