package pl.edu.uj.jnode.context.testdata;

import pl.edu.uj.jnode.context.InjectContext;
import pl.edu.uj.jnode.userlib.Task;

import java.io.Serializable;

public class RawTask implements Task {
    @InjectContext
    private ContextClass contextClass;

    public ContextClass getContextClass() {
        return contextClass;
    }

    @Override
    public Serializable call() throws Exception {
        return null;
    }
}
