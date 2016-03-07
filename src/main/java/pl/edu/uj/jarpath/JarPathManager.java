package pl.edu.uj.jarpath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.ApplicationShutdownEvent;
import pl.edu.uj.engine.event.CancelJarJobsEvent;
import pl.edu.uj.engine.event.JarJobsCompletedEvent;
import pl.edu.uj.engine.event.JarJobsExecutionStartedEvent;
import pl.edu.uj.options.JarOptionEvent;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by michal on 28.10.15.
 */

@Component
public class JarPathManager {

    public static final String currentNodeId = "CURRENT_NODE";
    @Autowired
    private JarPathServices jarPathServices;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    public void storeJarGivenByJarOptionAndCreateItsProperties(JarOptionEvent event) {
        List<Path> paths = event.getPaths();
        for (Path path : paths) {
            if (Files.notExists(path) || !jarPathServices.isJar(path)) {
                eventPublisher.publishEvent(new ApplicationShutdownEvent(this,
                        ApplicationShutdownEvent.ShutdownReason.INVALID_JAR_FILE,
                        "Not a jar file: " + path));
                return;
            }
        }

        for (Path jarFile : paths) {
            try {
                InputStream inputStream = Files.newInputStream(jarFile);
                storeJarWithProperties(jarFile, inputStream, "NODE_ID_STUB"); //TODO: set real node identifier;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void storeJarWithProperties(Path jarFile, InputStream jarData, String nodeId) {
        try {
            Path jarPath = jarPathServices.getJarPath();
            jarFile = getJarFileNameOnCluster(nodeId, jarFile.toString());
            JarProperties.fromJarPath(jarPath, nodeId).store();
            Files.copy(jarData, jarPath.resolve(jarFile.getFileName()), REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getJarFileNameOnCluster(String nodeId, String jarFileName) {
        if (nodeId.equals(currentNodeId))
            return Paths.get(nodeId);
        return Paths.get("EXT" + "__" + nodeId + "__" + jarFileName);
    }

    public void onCreateJar(Path pathToJar) {
        Path propertiesPath = jarPathServices.getPropertyForJar(pathToJar);
        if (Files.notExists(propertiesPath)) {
            //If path doesn't exists then user dropped jar (sorry).
            JarProperties.fromJarPath(pathToJar, currentNodeId)
                    .store();
        }
        eventPublisher.publishEvent(new JarStateChangedEvent(this, pathToJar.getFileName(), JarProperties.fromJarPath(pathToJar)));
    }

    public void onDeleteJar(Path pathToJar) {
        Path jarProperties = jarPathServices.getPropertyForJar(pathToJar);
        System.out.println("Deleting " + pathToJar);
        try {
            Files.deleteIfExists(jarProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
        eventPublisher.publishEvent(new JarDeletedEvent(this, pathToJar.getFileName()));

    }

    @EventListener
    public void onCancelJarJobsEvent(CancelJarJobsEvent event) {
        try {
            Path pathToJar = resolveFullPath(event.getJarFileName());
            JarProperties jarProperties = JarProperties.fromJarPath(pathToJar);
            jarProperties.setExecutionState(JarExecutionState.CANCELLED);
            jarProperties.store();
        } catch (Throwable e) {
            if (!(e instanceof IllegalStateException)) //When properties file is missing
                e.printStackTrace();
        }
    }

    @EventListener
    public void onJobExecutionStartedEvent(JarJobsExecutionStartedEvent event) {
        try {
            Path pathToJar = resolveFullPath(event.getJarName());
            JarProperties jarProperties = JarProperties.fromJarPath(pathToJar);
            jarProperties.setExecutionState(JarExecutionState.RUNNING);
            jarProperties.store();
        } catch (Throwable e) {
            if (!(e instanceof IllegalStateException)) //When properties file is missing
                e.printStackTrace();
        }
    }

    @EventListener
    public void onJarExecutionCompletedEvent(JarJobsCompletedEvent event) {
        try {
            Path pathToJar = resolveFullPath(event.getJarName());
            JarProperties jarProperties = JarProperties.fromJarPath(pathToJar);
            jarProperties.setExecutionState(JarExecutionState.COMPLETED);
            jarProperties.store();
        } catch (Throwable e) {
            if (!(e instanceof IllegalStateException)) //When properties file is missing
                e.printStackTrace();
        }
    }

    public void onDeleteProperties(Path propertiesPath) {
        Optional<Path> jarPath = jarPathServices.getJarForProperty(propertiesPath);
        if (!jarPath.isPresent())
            return;
        JarProperties jarProperties = JarProperties.fromJarPath(jarPath.get(), "UNKNOWN");
        jarProperties.store();
        eventPublisher.publishEvent(new JarPropertiesDeletedEvent(this, propertiesPath.getFileName(), jarPath.get().getFileName()));
    }

    public byte[] readJarContent(Path jarFileName) {
        Path pathToJar = resolveFullPath(jarFileName);
        if (!jarPathServices.isJar(jarFileName)) {
            return new byte[0];
        }
        try {
            return Files.readAllBytes(pathToJar);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path resolveFullPath(Path jarFileName) {
        return jarPathServices.getJarPath().resolve(jarFileName);
    }

    public boolean hasJar(String sourceNodeId, Path jarName) {
        jarName = getJarFileNameOnCluster(sourceNodeId, jarName.toString());
        return hasJar(jarName);
    }

    public boolean hasJar(Path jarName) {
        jarName = jarPathServices.getJarPath().resolve(jarName);
        return Files.exists(jarName) && Files.isReadable(jarName);
    }
}
