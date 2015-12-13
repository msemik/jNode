package pl.edu.uj.jarpath;

import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;

/**
 * Created by michal on 13.12.15.
 */
public class JarPropertiesDeletedEvent extends ApplicationEvent {
    private Path eventPath;
    private Path jarFileName;

    public JarPropertiesDeletedEvent(Object source, Path propertiesPath, Path jarFileName) {
        super(source);
        this.eventPath = propertiesPath;
        this.jarFileName = jarFileName;
    }

    public Path getPropertiesPath() {
        return eventPath;
    }

    public Path getJarFileName() {
        return jarFileName;
    }
}
