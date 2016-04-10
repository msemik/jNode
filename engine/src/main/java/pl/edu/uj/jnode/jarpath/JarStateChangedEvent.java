package pl.edu.uj.jnode.jarpath;

import org.springframework.context.ApplicationEvent;

/**
 * Created by michal on 22.10.15.
 */
public class JarStateChangedEvent extends ApplicationEvent {
    private final JarProperties properties;
    private Jar jar;

    public JarStateChangedEvent(Object source, Jar jar, JarProperties properties) {
        super(source);
        this.jar = jar;
        this.properties = properties;
    }

    public Jar getJar() {
        return jar;
    }

    public JarProperties getProperties() {
        return properties;
    }
}
