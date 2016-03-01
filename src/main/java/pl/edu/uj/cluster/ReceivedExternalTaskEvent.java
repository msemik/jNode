package pl.edu.uj.cluster;

import org.springframework.context.ApplicationEvent;

public class ReceivedExternalTaskEvent extends ApplicationEvent {

    public ReceivedExternalTaskEvent(Object source) {
        super(source);
    }
}
