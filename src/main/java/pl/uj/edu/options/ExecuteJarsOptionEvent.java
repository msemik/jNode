package pl.uj.edu.options;

import org.springframework.context.ApplicationEvent;

public class ExecuteJarsOptionEvent extends ApplicationEvent {

    public ExecuteJarsOptionEvent(Object source) {
        super(source);
    }
}