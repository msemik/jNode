package pl.edu.uj.jnode.context.testdata;

import pl.edu.uj.jnode.context.InjectContext;
import pl.edu.uj.jnode.userlib.Callback;

public class CallbackWithContextFields implements Callback {
    @InjectContext
    private ContextClass autowiredContextClass;
    private ContextClass notAutowiredContextClass;
    private Object nonContextClass;

    public Object getNonContextClass() {
        return nonContextClass;
    }

    public ContextClass getAutowiredContextClass() {
        return autowiredContextClass;
    }

    public void setAutowiredContextClass(ContextClass autowiredContextClass) {
        this.autowiredContextClass = autowiredContextClass;
    }

    public ContextClass getNotAutowiredContextClass() {
        return notAutowiredContextClass;
    }

    @Override
    public void onSuccess(Object taskResult) {

    }

    @Override
    public void onFailure(Throwable ex) {

    }
}
