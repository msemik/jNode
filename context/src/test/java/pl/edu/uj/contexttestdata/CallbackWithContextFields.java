package pl.edu.uj.contexttestdata;

import org.springframework.beans.factory.annotation.Autowired;
import pl.uj.edu.userlib.Callback;

public class CallbackWithContextFields implements Callback
{
    public void setAutowiredContextClass(ContextClass autowiredContextClass)
    {
        this.autowiredContextClass = autowiredContextClass;
    }

    @Autowired
    private ContextClass autowiredContextClass;
    private ContextClass notAutowiredContextClass;
    private Object nonContextClass;

    public Object getNonContextClass()
    {
        return nonContextClass;
    }

    public ContextClass getAutowiredContextClass()
    {
        return autowiredContextClass;
    }

    public ContextClass getNotAutowiredContextClass()
    {
        return notAutowiredContextClass;
    }

    @Override public void onSuccess(Object taskResult)
    {

    }

    @Override public void onFailure(Throwable ex)
    {

    }
}
