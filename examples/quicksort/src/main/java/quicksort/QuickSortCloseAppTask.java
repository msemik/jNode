package quicksort;

import pl.edu.uj.jnode.context.InjectContext;
import pl.edu.uj.jnode.userlib.Task;

import java.io.Serializable;

/**
 * Created by alanhawrot on 17.06.2016.
 */
public class QuickSortCloseAppTask implements Task {
    @InjectContext
    private QuickSortResultContext resultContext;

    @Override
    public Serializable call() throws Exception {
        System.out.println("Sorted array: " + resultContext.getResultAsString());
        return "";
    }
}
