package pl.uj.edu.jarpath;

import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;

/**
 * Created by michal on 22.10.15.
 */
public class JarReceivedEvent extends ApplicationEvent {
    public JarReceivedEvent(Path path, Object source) {
        super(source);
    }
}
