package quicksort;

import pl.edu.uj.jnode.context.Context;
import pl.edu.uj.jnode.userlib.TaskExecutor;
import pl.edu.uj.jnode.userlib.TaskExecutorFactory;

import java.util.Arrays;

/**
 * Created by alanhawrot on 14.04.2016.
 */
@Context
public class QuickSortResultContext {
    private int[] array;
    private boolean isSorted = false;
    private boolean isPrinted = false; // tmp

    public void copyResult(int[] array, int begin, int end) {
        if (this.array == null) {
            this.array = new int[array.length];
            System.arraycopy(array, 0, this.array, 0, array.length);
        } else {
            System.arraycopy(array, begin, this.array, begin, end - begin + 1);
        }
    }

    private boolean isSorted() {
        if (isSorted) {
            return true;
        }
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) {
                return false;
            }
        }
        return isSorted = true;
    }

    public void closeAppIfSorted() {
        if (isSorted() && !isPrinted) {
            isPrinted = true;
            TaskExecutor taskExecutor = TaskExecutorFactory.createTaskExecutor();
            taskExecutor.closeApp(new QuickSortCloseAppTask());
        }
    }

    public String getResultAsString() {
        return Arrays.toString(array);
    }
}
