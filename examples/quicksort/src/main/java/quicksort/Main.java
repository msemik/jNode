package quicksort;

import pl.edu.uj.jnode.context.ContextScan;
import pl.edu.uj.jnode.userlib.TaskExecutor;
import pl.edu.uj.jnode.userlib.TaskExecutorFactory;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by alanhawrot on 14.04.2016.
 */
@ContextScan("quicksort")
public class Main {
    public static void main(String[] args) {
        int[] array = new Random().ints(10000, 0, 50000).toArray();

        System.out.println("Array to sort: " + Arrays.toString(array));

        TaskExecutor taskExecutor = TaskExecutorFactory.createTaskExecutor();
        taskExecutor.doAsync(new QuickSortTask(array, 0, array.length - 1), new QuickSortCallback());
    }
}
