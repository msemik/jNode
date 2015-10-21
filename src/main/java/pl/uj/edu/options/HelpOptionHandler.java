package pl.uj.edu.options;

import org.apache.commons.cli.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by michal on 21.10.15.
 */
@Component
public class HelpOptionHandler {
    @Autowired
    private JNodeOptions jNodeOptions;

    //@org.springframework.context.event.EventListener
    public void onParseOptionsExc(HelpOptionEvent event) {
        jNodeOptions.printHelp();
    }

    //@org.springframework.context.event.EventListener
    public void onApplicationEvent(UnparsableArgumentsEvent event) {
        ParseException exception = event.getException();
        System.out.println("Error:" + exception.getMessage());
        jNodeOptions.printHelp();
    }
}
