package pl.edu.uj.main;

import org.springframework.context.ApplicationEvent;

/**
 * Created by michal on 21.10.15.
 */
public class ApplicationShutdownEvent extends ApplicationEvent {
    private String message;
    private ShutdownReason shutdownReason;

    public ApplicationShutdownEvent(Object source, ShutdownReason reason) {
        super(source);
        this.shutdownReason = reason;
    }

    public ApplicationShutdownEvent(Object source, ShutdownReason reason, String message) {
        super(source);
        this.message = message;
        this.shutdownReason = reason;
    }

    public String getMessage() {
        return message;
    }

    public ShutdownReason getShutdownReason() {
        return shutdownReason;
    }

    public void setShutdownReason(ShutdownReason shutdownReason) {
        this.shutdownReason = shutdownReason;
    }

    public enum ShutdownReason {UNPARSABLE_OPTIONS, WATCHER_SERVICE_ERROR, INVALID_JAR_FILE;}
}
