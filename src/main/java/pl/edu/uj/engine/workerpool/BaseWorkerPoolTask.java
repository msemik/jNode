package pl.edu.uj.engine.workerpool;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by michal on 22.11.15.
 */
public abstract class BaseWorkerPoolTask implements WorkerPoolTask {
    private String jarName;

    public BaseWorkerPoolTask(Path jarPath) {
        this.jarName = jarPath.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof WorkerPoolTask))
            return false;
        WorkerPoolTask task = (WorkerPoolTask) o;
        return getTaskId() == task.getTaskId();
    }

    @Override
    public String toString() {
        return "WorkerPoolTask{" +
                "task=" +
                " jarName=" + getJarName() +
                ", taskId=" + getTaskId() +
                '}';
    }

    @Override
    public Path getJarName() {
        return Paths.get(jarName);
    }

    @Override
    public int getTaskId() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean isExternal() {
        return false;
    }
}
