package pl.edu.uj.jnode.engine.workerpool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.engine.eventloop.EventLoopThread;
import pl.edu.uj.jnode.engine.eventloop.EventLoopThreadRegistry;

import java.io.Serializable;
import java.util.Optional;

/**
 * Created by alanhawrot on 17.06.2016.
 */
@Component
@Scope("prototype")
public class CloseAppTask extends WorkerPoolTaskDecorator {
    @Autowired
    private EventLoopThreadRegistry eventLoopThreadRegistry;

    public CloseAppTask(WorkerPoolTask task) {
        super(task);
        setMaxPriority();
    }

    @Override
    public Serializable call() throws Exception {
        Optional<EventLoopThread> elt = eventLoopThreadRegistry.get(getJar());
        if (!elt.isPresent()) {
            return null;
        }
        if (!elt.get().closeApp()) {
            return super.call();
        }
        return null;
    }

    @Override
    public boolean isClosingApp() {
        return true;
    }
}
