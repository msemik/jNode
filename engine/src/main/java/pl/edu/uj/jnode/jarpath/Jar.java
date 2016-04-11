package pl.edu.uj.jnode.jarpath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import pl.edu.uj.jnode.crosscuting.classloader.ChildFirstJarClassLoader;
import pl.edu.uj.jnode.engine.InvalidJarFileException;
import pl.edu.uj.jnode.engine.NodeIdFactory;
import pl.edu.uj.jnode.engine.UserApplicationException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Component
@Scope(scopeName = "prototype")
public class Jar {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private NodeIdFactory nodeIdFactory;
    @Autowired
    private JarPathServices jarPathServices;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    private String nodeId;
    private Path pathRelativeToJarPath;
    private ChildFirstJarClassLoader childFirstJarClassLoader;
    private Class<?> mainClass;

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

    @Override
    public String toString() {
        return getPathRelativeToJarPath().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Jar jar = (Jar) o;

        if (!nodeId.equals(jar.nodeId))
            return false;
        return pathRelativeToJarPath.equals(jar.pathRelativeToJarPath);

    }

    @Override
    public int hashCode() {
        int result = nodeId.hashCode();
        result = 31 * result + pathRelativeToJarPath.hashCode();
        return result;
    }

    public Class<?> getClass(String canonicalName) {
        ClassLoader classLoader = getChildFirstClassLoader();
        try {
            return Class.forName(canonicalName, true, classLoader);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public Optional<Class<Annotation>> getAnnotation(String canonicalName) {
        Class<?> cls = getClass(canonicalName);
        if (cls == null)
            return empty();
        if (!cls.isAnnotation()) {
            throw new IllegalStateException("Given type is not annotation: " + canonicalName);
        }
        return of((Class<Annotation>) cls);
    }

    public Object launchMain() {
        Class<?> mainClass = getMainClass();
        try {
            Method main = mainClass.getMethod("main", String[].class);
            String[] args = new String[0];
            return main.invoke(null, new Object[]{args});
        } catch (InvocationTargetException e) {
            throw new UserApplicationException(e.getCause());
        } catch (NoSuchMethodException e) {
            String message = "Declared main class doesn't have proper main method:" + e.getMessage();
            throw new InvalidJarFileException(message, e);
        } catch (IllegalAccessException e) {
            String message = "Declared main class is not accessible:" + e.getMessage();
            throw new InvalidJarFileException(message, e);
        }
    }

    public Class<?> getMainClass() {

        if (this.mainClass != null)
            return mainClass;
        try {
            ClassLoader classLoader = getChildFirstClassLoader();
            String mainClassName = getMainClassName();
            mainClass = classLoader.loadClass(mainClassName);
            return mainClass;
        } catch (ClassNotFoundException e) {
            String message = "Declared main class doesn't exist:" + e.getMessage();
            throw new InvalidJarFileException(message, e);
        }
    }

    public ClassLoader getChildFirstClassLoader() {
        return getChildFirstClassLoaderAndCreateIfNeed();
    }

    public ClassLoader getJarOnlyClassLoader() {
        return getChildFirstClassLoaderAndCreateIfNeed().getChildOnlyJarClassLoader();
    }

    private ChildFirstJarClassLoader getChildFirstClassLoaderAndCreateIfNeed() {
        if (childFirstJarClassLoader != null)
            return childFirstJarClassLoader;
        childFirstJarClassLoader = new ChildFirstJarClassLoader(getAbsolutePathAsString());
        return childFirstJarClassLoader;
    }

    private String getMainClassName() {
        try {
            JarFile jarFile = new JarFile(getAbsolutePathAsString());
            Manifest manifest = jarFile.getManifest();
            Attributes mainAttributes = manifest.getMainAttributes();

            for (Iterator it = mainAttributes.keySet().iterator(); it.hasNext(); ) {
                Attributes.Name attribute = (Attributes.Name) it.next();
                if (attribute.toString().equals("Main-Class"))
                    return (String) mainAttributes.get(attribute);
            }

            throw new InvalidJarFileException("Jar doesn't contain Main-Class attribute");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
