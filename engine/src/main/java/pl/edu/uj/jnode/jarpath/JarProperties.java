package pl.edu.uj.jnode.jarpath;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by michal on 30.10.15.
 */
public class JarProperties {
    private String nodeId;
    private JarExecutionState executionState;
    private Path propertiesPath;

    private JarProperties(Path propertiesPath, String nodeId, boolean loadIfExists) {
        this.nodeId = nodeId;
        this.propertiesPath = propertiesPath;
        this.executionState = JarExecutionState.NOT_STARTED;

        if (loadIfExists && isStored()) {
            Properties p = new Properties();
            try {
                p.load(Files.newInputStream(propertiesPath));
                executionState = JarExecutionState.valueOf(p.getProperty("executionState"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isStored() {
        return Files.exists(propertiesPath) && Files.isReadable(propertiesPath);
    }

    public static JarProperties createFor(Jar jar) {
        return createFor(jar, false);
    }

    private static JarProperties createFor(Jar jar, boolean loadPersistentPropertiesIfExists) {
        Path pathToJar = jar.getAbsolutePath();
        Path propertiesPath = convertJarPathToPropertiesPath(pathToJar);
        return new JarProperties(propertiesPath, jar.getNodeId(), loadPersistentPropertiesIfExists);
    }

    private static Path convertJarPathToPropertiesPath(Path pathToJar) {
        String pathToJarStr = pathToJar.toString();
        return Paths.get(pathToJarStr.substring(0, pathToJarStr.length() - 3) + "properties");
    }

    public static JarProperties readFor(Jar jar) {
        return createFor(jar, true);
    }

    @Override
    public String toString() {
        return "JarProperties{ propertiesPath=" + propertiesPath +
               ", nodeId='" + nodeId + '\'' +
               ", executionState=" + executionState +
               '}';
    }

    public JarExecutionState getExecutionState() {
        return executionState;
    }

    public void setExecutionState(JarExecutionState executionState) {
        this.executionState = executionState;
    }

    public void store() {
        Properties p = new Properties();
        p.setProperty("nodeId", nodeId);
        p.setProperty("executionState", executionState.toString());
        try {
            OutputStream propertiesStream = Files.newOutputStream(propertiesPath);
            p.store(propertiesStream, "jar path properties");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete() {
        try {
            Files.deleteIfExists(propertiesPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
