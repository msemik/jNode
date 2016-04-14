package pl.edu.uj.jnode.context.testdata;

import pl.edu.uj.jnode.context.InjectContext;
import pl.edu.uj.jnode.userlib.Task;

public class RawTask implements Task {
    @InjectContext
    private ContextClass contextClass;

    public ContextClass getContextClass() {
        return contextClass;
    }

    @Override
    public Object call() throws Exception {
        return null;
    }
}
