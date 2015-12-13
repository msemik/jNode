package pl.edu.uj.engine;

import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;

/**
 * Created by michal on 13.12.15.
 */
public class JarJobsExecutionStartedEvent extends ApplicationEvent {
    private Path jarName;

    public JarJobsExecutionStartedEvent(Object source, Path jarName) {
        super(source);
        this.jarName = jarName;
    }

    public Path getJarName() {
        return jarName;
    }
}
