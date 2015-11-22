package pl.edu.uj.jarpath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static java.nio.file.Files.notExists;

/**
 * Created by michal on 30.10.15.
 */
public class JarProperties {
    private final Path pathToJar;
    private String nodeId;
    private JarExecutionState executionState;

    private JarProperties(Path pathToJar, String nodeId) {
        this.nodeId = nodeId;
        this.pathToJar = pathToJar;
        executionState = JarExecutionState.NOT_STARTED;
    }

    public static JarProperties fromJarPath(Path pathToJar, String nodeId) {
        return new JarProperties(pathToJar, nodeId);
    }

    public static JarProperties fromJarPath(Path pathToJar) {
        Path propertiesPath = jarPathToPropertiesPath(pathToJar);
        if (notExists(propertiesPath) || !Files.isReadable(propertiesPath))
            throw new IllegalStateException("Can't read properties: " + propertiesPath);

        Properties p = new Properties();
        try {
            p.load(Files.newInputStream(propertiesPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JarProperties jarProperties = new JarProperties(pathToJar, p.getProperty("nodeId"));
        jarProperties.setExecutionState(JarExecutionState.CANCELLED.valueOf(p.getProperty("executionState")));
        return jarProperties;
    }

    private static Path jarPathToPropertiesPath(Path jarPath) {
        String s = jarPath.toString();
        return Paths.get(s.substring(0, s.length() - 3) + "properties");
    }

    public JarExecutionState getExecutionState() {
        return executionState;
    }

    public void setExecutionState(JarExecutionState executionState) {
        this.executionState = executionState;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void store() {
        Properties p = new Properties();
        p.setProperty("nodeId", nodeId);
        p.setProperty("executionState", executionState.toString());
        try {
            p.store(Files.newOutputStream(jarPathToPropertiesPath(pathToJar)), "jar path properties");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "JarProperties{ pathToJar=" + pathToJar +
                ", nodeId='" + nodeId + '\'' +
                ", executionState=" + executionState +
                '}';
    }
}
