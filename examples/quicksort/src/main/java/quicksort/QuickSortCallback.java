package quicksort;

import pl.edu.uj.jnode.context.InjectContext;
import pl.edu.uj.jnode.userlib.Callback;

/**
 * Created by alanhawrot on 14.04.2016.
 */
public class QuickSortCallback implements Callback {
    @InjectContext
    private QuickSortResultContext resultContext;

    @Override
    public void onSuccess(Object taskResult) {
        QuickSortTaskResult result = (QuickSortTaskResult) taskResult;
        resultContext.copyResult(result.getArray(), result.getBegin(), result.getEnd());
        resultContext.printResultIfSorted();
    }

    @Override
    public void onFailure(Throwable ex) {
        System.out.println(ex.getMessage());
    }
}
