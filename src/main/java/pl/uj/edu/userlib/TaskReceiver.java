package pl.uj.edu.userlib;

/**
 * Created by alanhawrot on 15.11.2015.
 */
public interface TaskReceiver {

    void doAsync(Task task, Callback callback);
}
