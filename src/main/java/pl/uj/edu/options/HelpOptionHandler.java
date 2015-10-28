package pl.uj.edu.options;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.uj.edu.ApplicationShutdownEvent;

import static pl.uj.edu.ApplicationShutdownEvent.ShutdownReason.UNPARSABLE_OPTIONS;

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
        System.out.println("Error:" + ((Exception) event.getCargo()).getMessage());
        jNodeOptions.printHelp();
    }
}
