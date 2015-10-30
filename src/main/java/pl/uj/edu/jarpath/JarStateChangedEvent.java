package pl.uj.edu.jarpath;

import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;

/**
 * Created by michal on 22.10.15.
 */
public class JarStateChangedEvent extends ApplicationEvent {
    private Path path;
    private final JarProperties properties;

    public JarStateChangedEvent(Object source, Path path, JarProperties properties) {
        super(source);
        this.path = path;
        this.properties = properties;
    }

    public Path getPath() {
        return path;
    }

    public JarProperties getProperties() {
        return properties;
    }
}
