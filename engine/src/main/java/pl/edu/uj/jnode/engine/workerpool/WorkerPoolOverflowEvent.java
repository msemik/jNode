package pl.edu.uj.jnode.engine.workerpool;

import org.springframework.context.ApplicationEvent;

/**
 * Created by alanhawrot on 29.02.2016.
 */
public class WorkerPoolOverflowEvent extends ApplicationEvent {
    public WorkerPoolOverflowEvent(Object source) {
        super(source);
    }
}
