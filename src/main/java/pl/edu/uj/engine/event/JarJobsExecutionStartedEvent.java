package pl.edu.uj.engine.event;

import org.springframework.context.ApplicationEvent;
import pl.edu.uj.jarpath.Jar;

/**
 * Created by michal on 13.12.15.
 */
public class JarJobsExecutionStartedEvent extends ApplicationEvent {
    private Jar jar;

    public JarJobsExecutionStartedEvent(Object source, Jar jar) {
        super(source);
        this.jar = jar;
    }

    public Jar getJar() {
        return jar;
    }
}
