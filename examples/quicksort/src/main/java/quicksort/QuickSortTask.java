package quicksort;

import pl.edu.uj.jnode.userlib.Task;
import pl.edu.uj.jnode.userlib.TaskExecutor;
import pl.edu.uj.jnode.userlib.TaskExecutorFactory;

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
    public Object call() throws Exception {
        TaskExecutor taskExecutor = TaskExecutorFactory.createTaskExecutor();
        quickSort(array, begin, end, taskExecutor);
        return new QuickSortTaskResult(array, begin, end);
    }

    private void quickSort(int[] array, int lo, int hi, TaskExecutor taskExecutor) {
        if (hi - lo <= 100) {
            insertionSort(array, lo, hi);
            return;
        }

        int mid = (lo + hi) / 2;
        int j = partition(array, lo, hi);
        if (j < mid) {
            taskExecutor.doAsync(new QuickSortTask(array, j + 1, hi), new QuickSortCallback());
            quickSort(array, lo, j - 1, taskExecutor);
        } else {
            taskExecutor.doAsync(new QuickSortTask(array, lo, j - 1), new QuickSortCallback());
            quickSort(array, j + 1, hi, taskExecutor);
        }
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
            for (int j = i; j > 0 && array[j] < array[j - 1]; j--) {
                swap(array, j, j - 1);
            }
        }
    }

    private void swap(int[] array, int i, int j) {
        int tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }
}
