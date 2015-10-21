package pl.uj.edu.options;

import org.apache.commons.cli.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
//import org.springframework.context.event.EventListener;
/**
 * Created by michal on 21.10.15.
 */
@Component
public class OptionsEventsDispatcher {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private JNodeOptions jNodeOptions;


    public OptionsEventsDispatcher() {
    }

    public void dispatchOptionsEvents(String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(jNodeOptions.getOptions(), args);
        } catch (ParseException e) {
            eventPublisher.publishEvent(new UnparsableArgumentsEvent(this, e));
        }
        if (cmd.hasOption("h"))
            eventPublisher.publishEvent(new HelpOptionEvent(this));
        if (cmd.hasOption("j"))
            eventPublisher.publishEvent(new ExecuteJarsOptionEvent(this));

    }

}
