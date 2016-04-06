package pl.edu.uj.main.options;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.main.ApplicationShutdownEvent;

import static pl.edu.uj.main.ApplicationShutdownEvent.ShutdownReason.UNPARSABLE_OPTIONS;

/**
 * Created by michal on 21.10.15.
 */
@Component
public class HelpOptionHandler {
    @Autowired
    private JNodeOptions jNodeOptions;

    @EventListener
    public void onParseOptionsExc(HelpOptionEvent event) {
        jNodeOptions.printHelp();
    }

    @EventListener
    public void onApplicationEvent(ApplicationShutdownEvent event) {
        if (event.getShutdownReason() != UNPARSABLE_OPTIONS)
            return;
        jNodeOptions.printHelp();
    }
}
