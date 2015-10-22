package pl.uj.edu.options;

import org.springframework.context.ApplicationEvent;

/**
 * Created by michal on 21.10.15.
 */
public class ApplicationShutdownEvent extends ApplicationEvent {
    public enum ShutdownReason {UNPARSABLE_OPTIONS, WATCHER_SERVICE_ERROR;}

    private ShutdownReason shutdownReason;
    private Object cargo;


    public ApplicationShutdownEvent(OptionsEventsDispatcher optionsEventsDispatcher, ShutdownReason reason) {
        super(optionsEventsDispatcher);
        this.shutdownReason = reason;
    }

    public ApplicationShutdownEvent(OptionsEventsDispatcher optionsEventsDispatcher, ShutdownReason reason, Object cargo) {
        super(optionsEventsDispatcher);
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
