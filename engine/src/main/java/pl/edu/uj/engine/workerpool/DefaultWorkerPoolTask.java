package pl.edu.uj.engine.workerpool;

import pl.edu.uj.jarpath.Jar;
import pl.uj.edu.userlib.Task;

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
