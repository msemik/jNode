package pl.edu.uj.jnode.context;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContextScan
{
    /**
     * @return base packages
     */
    String[] value();
}
