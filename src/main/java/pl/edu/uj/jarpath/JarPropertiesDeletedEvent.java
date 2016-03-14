package pl.edu.uj.jarpath;

import org.springframework.context.ApplicationEvent;

/**
 * Created by michal on 13.12.15.
 */
public class JarPropertiesDeletedEvent extends ApplicationEvent {
    private Jar jar;

    public JarPropertiesDeletedEvent(Object source, Jar jar) {
        super(source);
        this.jar = jar;
    }

    public Jar getJar() {
        return jar;
    }
}
