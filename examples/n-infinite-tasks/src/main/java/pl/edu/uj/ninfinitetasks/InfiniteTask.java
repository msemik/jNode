package pl.edu.uj.ninfinitetasks;

import pl.edu.uj.jnode.userlib.Task;

import java.io.Serializable;

/**
 * Created by michal on 2016-06-29.
 */
public class InfiniteTask implements Task {
    private int i;

    public InfiniteTask(int i) {

        this.i = i;
    }

    @Override
    public Serializable call() throws Exception {
        while (true) {
            System.out.println("Task " + i + " is executing...");
            Thread.sleep(3000);
        }
    }
}
