package pl.edu.uj.jnode.engine.workerpool;

import pl.edu.uj.jnode.jarpath.Jar;

/**
 * Created by michal on 22.11.15.
 */
public abstract class BaseWorkerPoolTask implements WorkerPoolTask {
    private transient Jar jar;
    private transient int priority = 0;

    public BaseWorkerPoolTask(Jar jar) {
        this.jar = jar;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof WorkerPoolTask)) {
            return false;
        }
        WorkerPoolTask task = (WorkerPoolTask) o;
        return getTaskId() == task.getTaskId();
    }

    @Override
    public String toString() {
        return "BaseWorkerPoolTask{" +
                "jar=" + jar +
                ", priority=" + priority +
                '}';
    }

    @Override
    public Jar getJar() {
        return jar;
    }

    public void setJar(Jar jar) {
        this.jar = jar;
    }

    @Override
    public long getTaskId() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean isExternal() {
        return false;
    }

    @Override
    public boolean belongToJar(Jar jar) {
        return this.jar.equals(jar);
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void incrementPriority() {
        priority++;
    }
}
