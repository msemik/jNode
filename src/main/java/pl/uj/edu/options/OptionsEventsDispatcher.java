package pl.uj.edu.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pl.uj.edu.ApplicationShutdownEvent;

import static pl.uj.edu.ApplicationShutdownEvent.ShutdownReason.UNPARSABLE_OPTIONS;
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

            if (cmd.hasOption("h"))
                eventPublisher.publishEvent(new HelpOptionEvent(this));
            if (cmd.hasOption("j"))
                eventPublisher.publishEvent(new JarOptionEvent(this, cmd.getOptionValues("j")));

        } catch (ParseException e) {
            eventPublisher.publishEvent(new ApplicationShutdownEvent(this, UNPARSABLE_OPTIONS,
                    e.getMessage()));
        }

    }

}
