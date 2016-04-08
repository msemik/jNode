package pl.edu.uj.engine.events;

import org.springframework.context.ApplicationEvent;
import pl.edu.uj.jarpath.Jar;

/**
 * Created by michal on 13.12.15.
 */
public class JarJobsCompletedEvent extends ApplicationEvent {
    private Jar jar;

    public JarJobsCompletedEvent(Object source, Jar jar) {
        super(source);
        this.jar = jar;
    }

    public Jar getJar() {
        return jar;
    }
}
