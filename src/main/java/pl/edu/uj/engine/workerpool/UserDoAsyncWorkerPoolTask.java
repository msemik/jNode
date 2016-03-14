package pl.edu.uj.engine.workerpool;

import pl.edu.uj.jarpath.Jar;
import pl.uj.edu.userlib.Task;

/**
 * Created by michal on 22.11.15.
 */
public class UserDoAsyncWorkerPoolTask extends BaseWorkerPoolTask {
    private final Task task;

    public UserDoAsyncWorkerPoolTask(Task task, Jar jar) {
        super(jar);
        this.task = task;
    }

    @Override
    public Object call() throws Exception {
        return task.call();
    }

}
