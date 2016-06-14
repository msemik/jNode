package pl.edu.uj.jnode.main.options;

import org.springframework.context.ApplicationEvent;

public class InitialHostsOptionEvent extends ApplicationEvent {

    private String initialHosts;

    public InitialHostsOptionEvent(Object src, String initialHosts) {
        super(src);
        this.initialHosts = initialHosts;
    }

    public String getInitialHosts() {
        return initialHosts;
    }
}
