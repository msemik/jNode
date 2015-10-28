package pl.uj.edu;

import org.springframework.context.ApplicationEvent;

/**
 * Created by michal on 21.10.15.
 */
public class ApplicationShutdownEvent extends ApplicationEvent {
    public enum ShutdownReason {UNPARSABLE_OPTIONS, WATCHER_SERVICE_ERROR, INVALID_JAR_FILE;}

    private ShutdownReason shutdownReason;
    private Object cargo;


    public ApplicationShutdownEvent(Object source, ShutdownReason reason) {
        super(source);
        this.shutdownReason = reason;
    }

    public ApplicationShutdownEvent(Object source, ShutdownReason reason, Object cargo) {
        super(source);
        this.cargo = cargo;
        this.shutdownReason = reason;
    }

    public Object getCargo() {
        return cargo;
    }

    public void setCargo(Object cargo) {
        this.cargo = cargo;
    }

    public ShutdownReason getShutdownReason() {
        return shutdownReason;
    }

    public void setShutdownReason(ShutdownReason shutdownReason) {
        this.shutdownReason = shutdownReason;
    }

}
