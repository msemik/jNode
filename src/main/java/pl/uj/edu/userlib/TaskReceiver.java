package pl.uj.edu.userlib;

import pl.uj.edu.engine.workerpool.WorkerPoolTask;

/**
 * Created by alanhawrot on 15.11.2015.
 */
public interface TaskReceiver {

    void doAsync(WorkerPoolTask task, Callback callback);
}
