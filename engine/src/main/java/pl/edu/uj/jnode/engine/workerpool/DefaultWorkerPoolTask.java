package pl.edu.uj.jnode.engine.workerpool;

import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.userlib.Task;

import java.io.Serializable;

/**
 * Created by michal on 22.11.15.
 */
public class DefaultWorkerPoolTask extends BaseWorkerPoolTask {
    private final Task task;

    public DefaultWorkerPoolTask(Task task, Jar jar, String nodeId) {
        super(jar, nodeId);
        this.task = task;
    }

    @Override
    public Serializable call() throws Exception {
        return task.call();
    }

    @Override
    public Task getRawTask() {
        return task;
    }
}
