package pl.edu.uj.jarpath;

import org.springframework.context.ApplicationEvent;

/**
 * Created by michal on 22.10.15.
 */
public class JarDeletedEvent extends ApplicationEvent {
    private Jar jarPath;

    public JarDeletedEvent(Object source, Jar jarPath) {
        super(source);
        this.jarPath = jarPath;
    }

    public Jar getJarPath() {
        return jarPath;
    }

    public void setJarPath(Jar jarPath) {
        this.jarPath = jarPath;
    }
}
