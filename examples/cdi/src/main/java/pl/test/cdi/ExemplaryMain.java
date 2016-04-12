package pl.test.cdi;

import pl.edu.uj.jnode.context.ContextScan;
import pl.edu.uj.jnode.userlib.*;

import static pl.edu.uj.jnode.userlib.TaskExecutorFactory.*;

@ContextScan("pl.test.cdi")
public class ExemplaryMain
{
    public static void main(String[] args)
    {
        TaskExecutor taskExecutor = createTaskExecutor();
        taskExecutor.doAsync(new TaskWithDependency(), new MyCallback(taskExecutor));
    }

}
