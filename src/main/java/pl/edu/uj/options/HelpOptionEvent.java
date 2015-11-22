package pl.edu.uj.options;

import org.springframework.context.ApplicationEvent;

/**
 * Created by michal on 21.10.15.
 */
public class HelpOptionEvent extends ApplicationEvent {
    public HelpOptionEvent(Object source) {
        super(source);
    }
}
