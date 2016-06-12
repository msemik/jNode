package pl.edu.uj.jnode.userlib;

/**
 * Created by alanhawrot on 15.11.2015.
 */
public interface TaskExecutor {
    void doAsync(Task task, Callback callback);

    int getAvailableWorkers();

    Object getBean(Class<?> passwordCrackerContextClass);
}
