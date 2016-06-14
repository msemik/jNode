package pl.edu.uj.jnode.main.options;

import org.springframework.context.ApplicationEvent;

public class BindAddressOptionEvent extends ApplicationEvent {
    private String bindAddress;


    public BindAddressOptionEvent(Object src, String bindAddress) {
        super(src);
        this.bindAddress = bindAddress;
    }


    public String getBindAddress() {
        return bindAddress;
    }
}
