package pl.edu.uj.options;

import org.springframework.context.ApplicationEvent;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomJarPathEvent extends ApplicationEvent {

    private String customJarPath;

    public CustomJarPathEvent(Object source, String customJarPath) {
        super(source);
        this.customJarPath = customJarPath;
    }

    public Path getCustomJarPath() {
        return Paths.get(customJarPath);
    }

}
