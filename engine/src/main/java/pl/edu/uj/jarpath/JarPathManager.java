package pl.edu.uj.jarpath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.engine.NodeIdFactory;
import pl.edu.uj.engine.event.CancelJarJobsEvent;
import pl.edu.uj.engine.event.JarJobsCompletedEvent;
import pl.edu.uj.engine.event.JarJobsExecutionStartedEvent;
import pl.edu.uj.main.ApplicationShutdownEvent;
import pl.edu.uj.main.options.JarOptionEvent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static pl.edu.uj.jarpath.JarExecutionState.*;
import static pl.edu.uj.main.ApplicationShutdownEvent.ShutdownReason.INVALID_JAR_FILE;

/**
 * Created by michal on 28.10.15.
 */
@Component
public class JarPathManager {
    Logger logger = LoggerFactory.getLogger(JarPathManager.class);
    @Autowired
    private JarPathServices jarPathServices;
    @Autowired
    private NodeIdFactory nodeIdFactory;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private JarFactory jarFactory;

    @EventListener
    public void storeJarGivenByJarOptionAndCreateItsProperties(JarOptionEvent event) {
        for (Path pathToJarWhichWillBeCopied : event.getPaths()) {
            if (!jarPathServices.isValidExistingJar(pathToJarWhichWillBeCopied)) {
                String message = "Not a jar file: " + pathToJarWhichWillBeCopied;
                ApplicationShutdownEvent shutdownEvent = new ApplicationShutdownEvent(this, INVALID_JAR_FILE, message);
                eventPublisher.publishEvent(shutdownEvent);
                return;
            }

            try {
                InputStream inputStream = Files.newInputStream(pathToJarWhichWillBeCopied);
                Jar jar = jarFactory.getFor(pathToJarWhichWillBeCopied.getFileName());
                jar.storeDefaultProperties();
                jar.storeJarContent(inputStream);
                logger.info("Stored jar: " + jar);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void onFoundJarAfterStart(Jar jar) {
        JarProperties jarProperties = jar.storeExecutionState(NOT_STARTED);
        eventPublisher.publishEvent(new JarStateChangedEvent(this, jar, jarProperties));
    }

    public void onCreateJar(Jar jar) {
        JarProperties jarProperties = jar.storeDefaultProperties();
        eventPublisher.publishEvent(new JarStateChangedEvent(this, jar, jarProperties));
    }

    public void onDeleteJar(Jar jar) {
        jar.deleteProperties();
        System.out.println("Deleted " + jar);
        eventPublisher.publishEvent(new JarDeletedEvent(this, jar));
    }

    @EventListener
    public void onCancelJarJobsEvent(CancelJarJobsEvent event) {
        Jar jar = event.getJar();
        jar.storeExecutionState(CANCELLED);
    }

    @EventListener
    public void onJobExecutionStartedEvent(JarJobsExecutionStartedEvent event) {
        Jar jar = event.getJar();
        jar.storeExecutionState(RUNNING);
    }

    @EventListener
    public void onJarExecutionCompletedEvent(JarJobsCompletedEvent event) {
        Jar jar = event.getJar();
        jar.storeExecutionState(COMPLETED);
    }

    public void onDeleteProperties(Path propertiesPath) {
        Optional<Jar> optionalJar = jarPathServices.getJarForProperty(propertiesPath);
        if (!optionalJar.isPresent()) {
            return;
        }
        Jar jar = optionalJar.get();
        jar.storeDefaultProperties();
        eventPublisher.publishEvent(new JarPropertiesDeletedEvent(this, jar));
    }
}
