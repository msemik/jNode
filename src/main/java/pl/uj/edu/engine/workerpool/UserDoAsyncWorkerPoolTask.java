package pl.uj.edu.engine.workerpool;

import pl.uj.edu.userlib.Task;

import java.nio.file.Path;

/**
 * Created by michal on 22.11.15.
 */
public class UserDoAsyncWorkerPoolTask extends BaseWorkerPoolTask {
    private final Task task;

    public UserDoAsyncWorkerPoolTask(Task task, Path jarPath) {
        super(jarPath);
        this.task = task;
    }

    @Override
    public Object call() throws Exception {
        return task.call();
    }

}
