package pl.edu.uj.main.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pl.edu.uj.main.ApplicationShutdownEvent;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static pl.edu.uj.main.ApplicationShutdownEvent.ShutdownReason.UNPARSABLE_OPTIONS;
//import org.springframework.context.events.EventListener;

/**
 * Created by michal on 21.10.15.
 */
@Component
public class OptionsEventsDispatcher {
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private JNodeOptions jNodeOptions;
    private Optional<Integer> poolSize;

    public OptionsEventsDispatcher() {
    }

    public Optional<Integer> getPoolSize() {
        return poolSize;
    }

    public void dispatchOptionsEvents(String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(jNodeOptions.getOptions(), args);

            if (cmd.hasOption("h")) {
                eventPublisher.publishEvent(new HelpOptionEvent(this));
            }

            if (cmd.hasOption("n")) {
                eventPublisher.publishEvent(new NodeIdOptionEvent(this, cmd.getOptionValue("n")));
            }

            if (cmd.hasOption("z")) {
                eventPublisher.publishEvent(new CustomJarPathEvent(this, cmd.getOptionValue("z")));
            }

            if (cmd.hasOption("j")) {
                eventPublisher.publishEvent(new JarOptionEvent(this, cmd.getOptionValues("j")));
            }

            if (cmd.hasOption("p")) {
                int p = Integer.parseInt(cmd.getOptionValue("p"));
                if (p < 1) {
                    String message = "Invalid pool size: " + p;
                    eventPublisher.publishEvent(new ApplicationShutdownEvent(this, UNPARSABLE_OPTIONS, message));
                }
                this.poolSize = ofNullable(p);
                eventPublisher.publishEvent(new PoolSizeOptionEvent(p, this));
            }
        } catch (ParseException e) {
            eventPublisher.publishEvent(new ApplicationShutdownEvent(this, UNPARSABLE_OPTIONS, e.getMessage()));
        }
    }
}
