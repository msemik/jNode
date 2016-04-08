package pl.edu.uj.userlib;


/**
 * Created by alanhawrot on 15.11.2015.
 */
public interface TaskExecutor {

    void doAsync(Task task, Callback callback);
}
