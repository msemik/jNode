package pl.uj.edu.options;

import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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