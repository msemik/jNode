package quicksort;

import pl.edu.uj.jnode.userlib.Task;
import pl.edu.uj.jnode.userlib.TaskExecutor;
import pl.edu.uj.jnode.userlib.TaskExecutorFactory;

import java.io.Serializable;

/**
 * Created by alanhawrot on 14.04.2016.
 */
public class QuickSortTask implements Task {
    private int[] array;
    private int begin;
    private int end;

    public QuickSortTask(int[] array, int begin, int end) {
        this.array = array;
        this.begin = begin;
        this.end = end;
    }

    @Override
    public Serializable call() throws Exception {
        if (end - begin <= 100) {
            insertionSort(array, begin, end);
            return new QuickSortTaskResult(array, begin, end);
        }

        TaskExecutor taskExecutor = TaskExecutorFactory.createTaskExecutor();
        int j = partition(array, begin, end);
        taskExecutor.doAsync(new QuickSortTask(array, begin, j - 1), new QuickSortCallback());
        taskExecutor.doAsync(new QuickSortTask(array, j + 1, end), new QuickSortCallback());
        return new QuickSortTaskResult(array, j, j);
    }

    private int partition(int[] array, int lo, int hi) {
        int i = lo;
        int j = hi + 1;
        int v = array[lo];
        while (true) {
            while (array[++i] < v) {
                if (i == hi) {
                    break;
                }
            }
            while (v < array[--j]) {
                if (j == lo) {
                    break;
                }
            }
            if (i >= j) {
                break;
            }
            swap(array, i, j);
        }
        swap(array, lo, j);
        return j;
    }

    private void insertionSort(int[] array, int lo, int hi) {
        for (int i = lo + 1; i <= hi; i++) {
            int x = array[i];
            int j = i - 1;
            while (j > 0 && array[j] > x) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = x;
        }
    }

    private void swap(int[] array, int i, int j) {
        int tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }
}
