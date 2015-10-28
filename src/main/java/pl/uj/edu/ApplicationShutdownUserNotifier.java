package pl.uj.edu;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Created by michal on 29.10.15.
 */
@Component
public class ApplicationShutdownUserNotifier {

    @EventListener
    public void onApplicationShutdown(ApplicationShutdownEvent event) {
        System.out.println("application closed: " + event.getShutdownReason().toString().toLowerCase().replace("_", " "));
    }
}
