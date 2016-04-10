package pl.edu.uj.jnode.jarpath;

import org.springframework.context.ApplicationEvent;

/**
 * Created by michal on 22.10.15.
 */
public class JarDeletedEvent extends ApplicationEvent {
    private Jar jar;

    public JarDeletedEvent(Object source, Jar jar) {
        super(source);
        this.jar = jar;
    }

    public Jar getJar() {
        return jar;
    }

    public void setJar(Jar jar) {
        this.jar = jar;
    }
}
