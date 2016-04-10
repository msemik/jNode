package pl.edu.uj.jnode.jarpath;

import org.springframework.context.ApplicationEvent;

public class NewJarCreatedEvent extends ApplicationEvent {
    private final Jar jar;

    public NewJarCreatedEvent(Object src, Jar jar) {
        super(src);
        this.jar = jar;
    }

    public Jar getJar() {
        return jar;
    }
}
