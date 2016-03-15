package pl.edu.uj.cluster.task;

import pl.edu.uj.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jarpath.Jar;

public class WorkerPoolTaskDecorator implements WorkerPoolTask {
    private WorkerPoolTask task;

    protected WorkerPoolTaskDecorator(WorkerPoolTask task) {
        this.task = task;
        if (task == null)
            throw new IllegalStateException("Can't create with null task!!");
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
    public Object call() throws Exception {
        return task.call();
    }

    public WorkerPoolTask getTask() {
        return task;
    }

    protected void setTask(WorkerPoolTask task) {
        this.task = task;
    }
}
