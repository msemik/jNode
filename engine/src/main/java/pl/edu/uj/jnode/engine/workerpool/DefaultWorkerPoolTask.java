package pl.edu.uj.jnode.engine.workerpool;

import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.userlib.Task;

/**
 * Created by michal on 22.11.15.
 */
public class DefaultWorkerPoolTask extends BaseWorkerPoolTask {
    private final Task task;

    public DefaultWorkerPoolTask(Task task, Jar jar) {
        super(jar);
        this.task = task;
    }

    @Override
    public Object call() throws Exception {
        return task.call();
    }
}
