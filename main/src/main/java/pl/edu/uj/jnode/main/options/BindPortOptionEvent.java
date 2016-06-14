package pl.edu.uj.jnode.main.options;

import org.springframework.context.ApplicationEvent;

public class BindPortOptionEvent extends ApplicationEvent {
    private String port;

    public BindPortOptionEvent(Object src, String port) {
        super(src);
        this.port = port;
    }

    public String getPort() {
        return port;
    }
}
