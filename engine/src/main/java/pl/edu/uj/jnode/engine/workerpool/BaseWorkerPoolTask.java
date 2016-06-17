package pl.edu.uj.jnode.engine.workerpool;

import pl.edu.uj.jnode.jarpath.Jar;

import java.util.UUID;

/**
 * Created by michal on 22.11.15.
 */
public abstract class BaseWorkerPoolTask implements WorkerPoolTask {
    private transient Jar jar;
    private transient int priority = 0;
    private String taskId;

    public BaseWorkerPoolTask(Jar jar, String nodeId) {
        this.jar = jar;
        this.taskId = nodeId.concat(UUID.randomUUID().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof WorkerPoolTask)) {
            return false;
        }
        WorkerPoolTask task = (WorkerPoolTask) o;
        return getTaskId().equals(task.getTaskId());
    }

    @Override
    public String toString() {
        return "BaseWorkerPoolTask{" +
               "jar=" + jar +
               ", priority=" + priority +
               ", taskId='" + taskId + '\'' +
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
    public String getTaskId() {
        return taskId;
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
        if (priority < Integer.MAX_VALUE) {
            priority++;
        }
    }

    @Override
    public void setMaxPriority() {
        priority = Integer.MAX_VALUE;
    }
}
