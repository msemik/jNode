package pl.edu.uj.options;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import pl.edu.uj.ApplicationShutdownEvent;
import pl.edu.uj.jarpath.Jar;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static pl.edu.uj.ApplicationShutdownEvent.ShutdownReason.INVALID_JAR_FILE;

public class JarOptionEvent extends ApplicationEvent {
    private List<Path> paths = new ArrayList<>();

    public JarOptionEvent(Object source, String[] stringPaths) {
        super(source);
        for (String stringPath : stringPaths) {
            paths.add(Paths.get(stringPath));
        }
    }

    public List<Path> getPaths() {
        return paths;
    }
}