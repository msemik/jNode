package pl.edu.uj.jnode.main;

import org.springframework.context.ApplicationEvent;

/**
 * Created by michal on 02.04.16. Dupa
 */
public class OptionsDispatchedEvent extends ApplicationEvent {
    public OptionsDispatchedEvent(Object src) {
        super(src);
    }
}
