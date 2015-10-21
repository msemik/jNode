package pl.uj.edu.options;

import org.apache.commons.cli.ParseException;
import org.springframework.context.ApplicationEvent;

/**
 * Created by michal on 21.10.15.
 */
public class UnparsableArgumentsEvent extends ApplicationEvent {
    private ParseException exception;

    public UnparsableArgumentsEvent(Object source) {
        super(source);
    }

    public UnparsableArgumentsEvent(OptionsEventsDispatcher optionsEventsDispatcher, ParseException e) {
        super(optionsEventsDispatcher);
        exception = e;
    }

    public ParseException getException() {
        return exception;
    }

    public void setException(ParseException exception) {
        this.exception = exception;
    }
}
