package pl.edu.uj.engine.event;

import org.springframework.context.ApplicationEvent;
import pl.edu.uj.jarpath.Jar;

/**
 * Created by michal on 13.12.15.
 */
public class CancelJarJobsEvent extends ApplicationEvent { // TODO: Add event origin
    private Jar jar;
    private CancellationEventOrigin origin = CancellationEventOrigin.INTERNAL;

    public CancelJarJobsEvent(Object source, Jar jarPath) {
        super(source);
        this.jar = jarPath;
    }

    public CancelJarJobsEvent(Object source, Jar jarPath, CancellationEventOrigin origin) {
        super(source);
        this.jar = jarPath;
    }

    public Jar getJar() {
        return jar;
    }

    public CancellationEventOrigin getOrigin() {
        return origin;
    }
}
