package pl.edu.uj.jarpath;

import org.springframework.context.ApplicationEvent;

/**
 * Created by michal on 22.10.15.
 */
public class JarStateChangedEvent extends ApplicationEvent {
    private final JarProperties properties;
    private Jar path;

    public JarStateChangedEvent(Object source, Jar path, JarProperties properties) {
        super(source);
        this.path = path;
        this.properties = properties;
    }

    public Jar getJar() {
        return path;
    }

    public JarProperties getProperties() {
        return properties;
    }
}
