package pl.uj.edu.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.uj.edu.jarpath.JarDeletedEvent;
import pl.uj.edu.jarpath.JarStateChangedEvent;

/**
 * Created by michal on 31.10.15.
 */
@Component
public class WorkersPool {
    Logger logger = LoggerFactory.getLogger(WorkersPool.class);

    @EventListener
    public void onJarStateChanged(JarStateChangedEvent event) {
        logger.error("Got jar " + event.getPath() + " with properties " + event.getProperties() + " perhaps we can start a job if executionState is not started?");
    }

    @EventListener
    public void onJarDeleted(JarDeletedEvent event) {
        logger.error("Deleted jar " + event.getJarPath() + " we may removed job if exists");
    }
}
