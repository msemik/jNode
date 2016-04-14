package pl.edu.uj.jnode.cluster.task;

import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.userlib.Task;

import java.io.Serializable;

public class WorkerPoolTaskDecorator implements WorkerPoolTask {
    private transient WorkerPoolTask task;

    protected WorkerPoolTaskDecorator(WorkerPoolTask task) {
        this.task = task;
        if (task == null) {
            throw new IllegalStateException("Can't create with null task!!");
        }
    }

    @Override
    public Jar getJar() {
        return task.getJar();
    }

    @Override
    public String getTaskId() {
        return task.getTaskId();
    }

    @Override
    public boolean isExternal() {
        return false;
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

    @Override
    public Task getRawTask() {
        return task.getRawTask();
    }

    @Override
    public Serializable call() throws Exception {
        return task.call();
    }

    public WorkerPoolTask getTask() {
        return task;
    }

    protected void setTask(WorkerPoolTask task) {
        this.task = task;
    }
}
