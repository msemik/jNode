package pl.edu.uj.jnode.engine.event;

import org.springframework.context.ApplicationEvent;
import pl.edu.uj.jnode.jarpath.Jar;

/**
 * Created by michal on 13.12.15.
 */
public class CancelJarJobsEvent extends ApplicationEvent {
    private Jar jar;
    private CancellationEventOrigin origin = CancellationEventOrigin.INTERNAL;

    public CancelJarJobsEvent(Object source, Jar jar) {
        super(source);
        this.jar = jar;
    }

    public CancelJarJobsEvent(Object source, Jar jar, CancellationEventOrigin origin) {
        super(source);
        this.jar = jar;
        this.origin = origin;
    }

    public Jar getJar() {
        return jar;
    }

    public CancellationEventOrigin getOrigin() {
        return origin;
    }
}
