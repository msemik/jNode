package pl.edu.uj.jnode.userlib;

/**
 * Created by alanhawrot on 15.11.2015.
 */
public interface TaskExecutor {
    void doAsync(Task task, Callback callback);

    void closeApp(Task preCloseAppTask);

    long getAvailableWorkers();

    long getTotalWorkers();

    Object getBean(Class<?> passwordCrackerContextClass);
}
