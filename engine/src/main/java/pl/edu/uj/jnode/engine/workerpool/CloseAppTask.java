package pl.edu.uj.jnode.engine.workerpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.engine.event.CancelJarJobsEvent;

import java.io.Serializable;

/**
 * Created by alanhawrot on 17.06.2016.
 */
@Component
@Scope("prototype")
public class CloseAppTask extends WorkerPoolTaskDecorator {
    private Logger logger = LoggerFactory.getLogger(CloseAppTask.class);
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public CloseAppTask(WorkerPoolTask task) {
        super(task);
        setMaxPriority();
    }

    @Override
    public Serializable call() throws Exception {
        Serializable result = super.call();
        logger.warn("Closing application on demand");
        eventPublisher.publishEvent(new CancelJarJobsEvent(this, getJar()));
        return result;
    }
}
