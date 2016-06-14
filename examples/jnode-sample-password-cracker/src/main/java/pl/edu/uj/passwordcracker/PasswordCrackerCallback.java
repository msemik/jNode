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
    private volatile boolean foundPassword = false;

    public PasswordCrackerCallback(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void onSuccess(Serializable taskResult) {
        if (taskResult != null) {
            foundPassword = true;
            System.out.println("PasswordCrackerCallback: Found password: " + taskResult);
        } else {
            if(foundPassword)
                return;
            long availableWorkers = taskExecutor.getAvailableWorkers();
            long totalWorkers = taskExecutor.getTotalWorkers();
            PasswordGenerator passwordGenerator = passwordCrackerContext.getPasswordGenerator();
            System.out.println("PasswordCrackerCallback: Scheduling " + (availableWorkers + 1) + " tasks");

            byte[] encryptedPassword = passwordCrackerContext.getEncryptedPassword();

            for (int i = 0; i < availableWorkers + 1; i++) {
                PasswordGenerator separatedJobsGenerator = passwordGenerator.separateJobSet();
                PasswordCrackerTask task = new PasswordCrackerTask(separatedJobsGenerator, encryptedPassword);
                taskExecutor.doAsync(task, this);
            }
        }
    }

    @Override
    public void onFailure(Throwable ex) {
        ex.printStackTrace();
    }
}
