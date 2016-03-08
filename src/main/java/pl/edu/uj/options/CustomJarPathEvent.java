package pl.edu.uj.options;

import org.springframework.context.ApplicationEvent;

public class CustomJarPathEvent extends ApplicationEvent {

    private String customJarPath;

    public CustomJarPathEvent(Object source, String customJarPath) {
        super(source);
        this.customJarPath = customJarPath;
    }

    public String getCustomJarPath() {
        return customJarPath;
    }

}
