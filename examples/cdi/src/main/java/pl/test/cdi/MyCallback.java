package pl.test.cdi;

import pl.edu.uj.jnode.context.InjectContext;
import pl.edu.uj.jnode.userlib.*;

class MyCallback implements Callback
{
    @InjectContext
    private Counter counter;
    private final TaskExecutor taskExecutor;

    public MyCallback(TaskExecutor taskExecutor)
    {
        this.taskExecutor = taskExecutor;
    }

    @Override public void onSuccess(Object o)
    {
        int i = counter.preInc();
        System.out.println("Inc " + i);
        if(i == 100)
            return;
        taskExecutor.doAsync(new TaskWithDependency(), this);

    }

    @Override public void onFailure(Throwable throwable)
    {

    }
}
