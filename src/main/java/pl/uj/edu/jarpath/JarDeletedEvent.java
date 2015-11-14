package pl.uj.edu.jarpath;

import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;

/**
 * Created by michal on 22.10.15.
 */
public class JarDeletedEvent extends ApplicationEvent {
    private Path jarPath;

    public JarDeletedEvent(Object source, Path jarPath) {
        super(source);
        this.jarPath = jarPath;
    }

    public Path getJarPath() {
        return jarPath;
    }

    public void setJarPath(Path jarPath) {
        this.jarPath = jarPath;
    }
}
