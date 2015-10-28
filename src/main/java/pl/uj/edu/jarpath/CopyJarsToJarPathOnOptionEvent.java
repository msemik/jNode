package pl.uj.edu.jarpath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.uj.edu.ApplicationShutdownEvent;
import pl.uj.edu.options.JarOptionEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by michal on 28.10.15.
 */

@Component
public class CopyJarsToJarPathOnOptionEvent {

    @Autowired
    private JarPathServices jarPathServices;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onJarOptionEvent(JarOptionEvent event) {
        List<Path> paths = event.getPaths();
        validatePathsAreJars(paths);

        Path jarPath = jarPathServices.getJarPath();
        for (Path path : paths) {
            try {
                Files.copy(path, jarPath.resolve(path.getFileName()), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void validatePathsAreJars(List<Path> paths) {
        for (Path path : paths) {
            if(Files.notExists(path) || !jarPathServices.isJar(path))
                eventPublisher.publishEvent(new ApplicationShutdownEvent(this, ApplicationShutdownEvent.ShutdownReason.INVALID_JAR_FILE));
        }
    }

}
