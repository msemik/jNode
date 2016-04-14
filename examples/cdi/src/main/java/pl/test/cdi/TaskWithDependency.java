package pl.test.cdi;

import pl.edu.uj.jnode.context.InjectContext;
import pl.edu.uj.jnode.userlib.Task;

public class TaskWithDependency implements Task {
    @InjectContext
    private Dependency dependency;

    @Override
    public Object call() throws Exception {
        System.out.println("Dependency:" + dependency);
        return null;
    }
}
