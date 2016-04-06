package pl.edu.uj.main;

import org.springframework.context.ApplicationEvent;

public class ApplicationInitializedEvent extends ApplicationEvent {
    public ApplicationInitializedEvent(Object src) {
        super(src);
    }
}
