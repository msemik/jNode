package pl.edu.uj.jnode.engine.event;

import org.springframework.context.ApplicationEvent;
import pl.edu.uj.jnode.jarpath.Jar;

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
