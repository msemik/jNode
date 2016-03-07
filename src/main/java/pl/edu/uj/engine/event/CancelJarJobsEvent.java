package pl.edu.uj.engine.event;

import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;

/**
 * Created by michal on 13.12.15.
 */
public class CancelJarJobsEvent extends ApplicationEvent { // TODO: Add event origin
    private Path jarFileName;
    private CancellationEventOrigin origin = CancellationEventOrigin.INTERNAL;

    public CancelJarJobsEvent(Object source, Path jarPath) {
        super(source);
        this.jarFileName = jarPath;
    }

    public CancelJarJobsEvent(Object source, Path jarPath, CancellationEventOrigin origin) {
        super(source);
        this.jarFileName = jarPath;
    }

    public Path getJarFileName() {
        return jarFileName;
    }

    public CancellationEventOrigin getOrigin() {
        return origin;
    }
}
