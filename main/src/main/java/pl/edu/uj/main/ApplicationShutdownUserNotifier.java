package pl.edu.uj.main;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Created by michal on 29.10.15.
 */
@Component
public class ApplicationShutdownUserNotifier {
    @EventListener
    public void onApplicationShutdown(ApplicationShutdownEvent event) {
        if (event.getMessage() != null) {
            System.out.println("application closed:" + event.getMessage());
        } else {
            System.out.println("application closed: " + event.getShutdownReason().toString().toLowerCase().replace("_", " "));
        }
    }
}
