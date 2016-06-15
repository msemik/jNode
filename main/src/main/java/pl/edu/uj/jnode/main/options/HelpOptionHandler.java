package pl.edu.uj.jnode.main.options;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.main.ApplicationShutdownEvent;

import java.util.Arrays;

import static pl.edu.uj.jnode.main.ApplicationShutdownEvent.ShutdownReason.PRINT_HELP_ONLY;
import static pl.edu.uj.jnode.main.ApplicationShutdownEvent.ShutdownReason.UNPARSABLE_OPTIONS;

/**
 * Created by michal on 21.10.15.
 */
@Component
public class HelpOptionHandler {
    @Autowired
    private JNodeOptions jNodeOptions;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onParseOptionsExc(HelpOptionEvent event) {
        jNodeOptions.printHelp();
        eventPublisher.publishEvent(new ApplicationShutdownEvent(this, PRINT_HELP_ONLY));
    }

    @EventListener
    public void onApplicationEvent(ApplicationShutdownEvent event) {
        if (Arrays.asList(UNPARSABLE_OPTIONS, PRINT_HELP_ONLY).contains(event.getShutdownReason())) {
            return;
        }
        jNodeOptions.printHelp();
    }
}
