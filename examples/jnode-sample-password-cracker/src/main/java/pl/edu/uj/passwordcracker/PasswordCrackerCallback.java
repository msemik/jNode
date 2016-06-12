package pl.edu.uj.passwordcracker;

import pl.edu.uj.jnode.context.*;
import pl.edu.uj.jnode.userlib.*;

import java.io.Serializable;

/**
 * Created by michal on 2016-06-12.
 */
public class PasswordCrackerCallback implements Callback {
    private final TaskExecutor taskExecutor;
    @InjectContext
    private PasswordCrackerContext passwordCrackerContext;

    public PasswordCrackerCallback(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void onSuccess(Serializable taskResult) {
        if (taskResult != null) {
            System.out.println("Found password: " + taskResult);
        } else {
            int availableWorkers = taskExecutor.getAvailableWorkers();
            PasswordGenerator passwordGenerator = passwordCrackerContext.getPasswordGenerator();

            for (int i = 0; i < availableWorkers + 1; i++) {
                PasswordGenerator separatedJobsGenerator = passwordGenerator.separateJobSet();
                PasswordCrackerTask task = new PasswordCrackerTask(separatedJobsGenerator);
                taskExecutor.doAsync(task, this);
            }
        }
    }

    @Override
    public void onFailure(Throwable ex) {
        ex.printStackTrace();
    }
}
