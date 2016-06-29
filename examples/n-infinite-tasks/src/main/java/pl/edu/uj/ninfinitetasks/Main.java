package pl.edu.uj.ninfinitetasks;

import pl.edu.uj.jnode.context.ContextScan;
import pl.edu.uj.jnode.userlib.*;

import java.io.Serializable;
import java.util.Scanner;

/**
 * Created by michal on 2016-06-29.
 */
@ContextScan("ninfinitetasks")
public class Main {
    public static void main(String[] args) {
        System.out.println("Provide n:");
        Scanner s = new Scanner(System.in);
        int n = s.nextInt();
        TaskExecutor taskExecutor = TaskExecutorFactory.createTaskExecutor();

        for (int i = 0; i < n; i++) {
            taskExecutor.doAsync(new InfiniteTask(i), new Callback() {
                @Override
                public void onSuccess(Serializable taskResult) {

                }

                @Override
                public void onFailure(Throwable ex) {

                }
            });
        }
    }
}
