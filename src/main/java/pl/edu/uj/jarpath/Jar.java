package pl.edu.uj.jarpath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.edu.uj.engine.JarLauncher;
import pl.edu.uj.engine.NodeIdFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Component
@Scope(scopeName = "prototype")
public class Jar {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private NodeIdFactory nodeIdFactory;
    @Autowired
    private JarPathServices jarPathServices;
    private String nodeId;
    private Path pathRelativeToJarPath;
    private JarLauncher jarLauncher;

    protected Jar(String nodeId, Path pathRelativeToJarPath) {
        this.nodeId = nodeId;
        this.pathRelativeToJarPath = pathRelativeToJarPath;
    }

    public void validate() {
        if (!pathRelativeToJarPath.toString().endsWith(".jar"))
            throw new IllegalArgumentException("Invalid jar path '" + pathRelativeToJarPath + "'");
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getAbsolutePathAsString() {
        return getAbsolutePath().toString();
    }

    public Path getAbsolutePath() {
        return jarPathServices.getJarPath().resolve(pathRelativeToJarPath);
    }

    public byte[] readContent() {
        if (!isValidExistingJar()) {
            return new byte[0];
        }
        try {
            return Files.readAllBytes(getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isValidExistingJar() {
        Path absolutePath = getAbsolutePath();
        return Files.exists(absolutePath) && Files.isReadable(absolutePath);
    }

    public void storeJarContent(InputStream jarContent) {
        try {

            Path absolutePath = getAbsolutePath();
            Path dir = absolutePath.getParent();
            if (!Files.exists(dir))
                Files.createDirectory(dir);
            Files.copy(jarContent, absolutePath, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JarProperties storeExecutionState(JarExecutionState executionState) {
        JarProperties jarProperties = readProperties();
        jarProperties.setExecutionState(executionState);
        jarProperties.store();
        return jarProperties;
    }

    public JarProperties readProperties() {
        return JarProperties.readFor(this);
    }

    public JarProperties storeDefaultProperties() {
        JarProperties jarProperties = JarProperties.createFor(this);
        jarProperties.store();
        return jarProperties;
    }

    public JarProperties deleteProperties() {
        JarProperties jarProperties = JarProperties.readFor(this);
        jarProperties.delete();
        return jarProperties;
    }

    public boolean isLocal() {
        return nodeId.equals(nodeIdFactory.getCurrentNodeId());
    }

    public ClassLoader getClassLoader() {
        return getJarLauncher().getClassLoader();
    }

    private JarLauncher getJarLauncher() {
        if (jarLauncher == null) {
            jarLauncher = applicationContext.getBean(JarLauncher.class);
            jarLauncher.setJar(this);
        }
        return jarLauncher;
    }

    public String getFileNameAsString() {
        return getFileName().toString();
    }

    public Path getFileName() {
        return pathRelativeToJarPath.getFileName();
    }

    public boolean hasRelativePath(Path pathToJar) {
        return getPathRelativeToJarPath().equals(pathToJar);
    }

    public Path getPathRelativeToJarPath() {
        return pathRelativeToJarPath;
    }

    public Object launchMain() {
        return getJarLauncher().launchMain();
    }

    @Override
    public String toString() {
        return getPathRelativeToJarPath().toString();
    }
}
