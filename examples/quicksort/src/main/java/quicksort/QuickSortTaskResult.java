package quicksort;

import java.io.Serializable;

/**
 * Created by alanhawrot on 14.04.2016.
 */
public class QuickSortTaskResult implements Serializable {
    private int[] array;
    private int begin;
    private int end;

    public QuickSortTaskResult(int[] array, int begin, int end) {
        this.array = array;
        this.begin = begin;
        this.end = end;
    }

    public int[] getArray() {
        return array;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }
}
