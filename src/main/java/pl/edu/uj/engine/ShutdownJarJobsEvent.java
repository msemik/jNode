package pl.edu.uj.engine;

import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;

/**
 * Created by michal on 13.12.15.
 */
public class ShutdownJarJobsEvent extends ApplicationEvent {
    private Path jarFileName;

    public ShutdownJarJobsEvent(Object source, Path jarPath) {
        super(source);
        this.jarFileName = jarPath;
    }

    public Path getJarFileName() {
        return jarFileName;
    }
}
