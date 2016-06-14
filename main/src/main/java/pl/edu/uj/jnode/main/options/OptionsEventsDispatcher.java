package pl.edu.uj.jnode.main.options;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.main.ApplicationShutdownEvent;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static pl.edu.uj.jnode.main.ApplicationShutdownEvent.ShutdownReason.UNPARSABLE_OPTIONS;
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
    private Optional<Integer> poolSize;

    public OptionsEventsDispatcher() {
    }

    public Optional<Integer> getPoolSize() {
        return poolSize;
    }

    public boolean dispatchOptionsEvents(String[] args) {
        boolean validInitialization = true;
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

            if (cmd.hasOption("b")) {
                eventPublisher.publishEvent(new BindAddressOptionEvent(this, cmd.getOptionValue("b")));
            }

            if (cmd.hasOption("p")) {
                eventPublisher.publishEvent(new BindPortOptionEvent(this, cmd.getOptionValue("p")));
            }

            if (cmd.hasOption("i")) {
                eventPublisher.publishEvent(new InitialHostsOptionEvent(this, cmd.getOptionValue("i")));
            }

            if (cmd.hasOption("s")) {
                int s = Integer.parseInt(cmd.getOptionValue("s"));
                String message = "Invalid pool size: " + s;
                if (s < 1) {
                    eventPublisher.publishEvent(new ApplicationShutdownEvent(this, UNPARSABLE_OPTIONS, message));
                    validInitialization = false;
                }
                this.poolSize = ofNullable(s);
                eventPublisher.publishEvent(new PoolSizeOptionEvent(s, this));
            }
        } catch (ParseException e) {
            eventPublisher.publishEvent(new ApplicationShutdownEvent(this, UNPARSABLE_OPTIONS, e.getMessage()));
            validInitialization = false;
        }
        return validInitialization;
    }
}
