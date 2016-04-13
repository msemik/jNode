package factorial;

import pl.edu.uj.jnode.context.ContextScan;
import pl.edu.uj.jnode.userlib.TaskExecutor;
import pl.edu.uj.jnode.userlib.TaskExecutorFactory;

/**
 * Created by alanhawrot on 13.04.2016.
 */
@ContextScan("factorial")
public class Main {
    public static void main(String[] args) {
        System.out.println("Factorial sample: 1000!");

        TaskExecutor taskExecutor = TaskExecutorFactory.createTaskExecutor();
        taskExecutor.doAsync(new FactorialTask(0, 1000), new FactorialCallback());
    }
}
