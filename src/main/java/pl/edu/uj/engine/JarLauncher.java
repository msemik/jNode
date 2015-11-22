package pl.edu.uj.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xeustechnologies.jcl.JarClassLoader;
import pl.edu.uj.jarpath.JarPathServices;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Created by michal on 31.10.15.
 */

@Component
@Scope("prototype")
public class JarLauncher {
    private Path path;
    private JarClassLoader jcl;

    @Autowired
    JarPathServices jarPathServices;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    public JarLauncher() {
    }

    private void validatePathIsReadable() {
        if (Files.notExists(path) || !Files.isReadable(path))
            throw new IllegalStateException("Unreadable path :" + path);
    }

    private String getMainClass(Path pathToJar) {
        try {
            JarFile jarFile = new JarFile(pathToJar.toString());
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

    public Object launchMain() {
        Path fileName = path.getFileName();
        try {
            ClassLoader classLoader = getClassLoader();
            String mainClassName = getMainClass(path);
            Class<?> mainClass = classLoader.loadClass(mainClassName);

            Method main = mainClass.getMethod("main", String[].class);
            String[] args = new String[0];
            return main.invoke(null, new Object[]{args});
        } catch (ClassNotFoundException e) {
            String message = "Declared main class doesn't exist:" + e.getMessage();
            throw new InvalidJarFileException(message, e);
        } catch (InvocationTargetException e) {
            String message = "Main method has thrown exception: " + e;
            throw new InvalidJarFileException(message, e);
        } catch (NoSuchMethodException e) {
            String message = "Declared main class doesn't have proper main method:" + e.getMessage();
            throw new InvalidJarFileException(message, e);
        } catch (IllegalAccessException e) {
            String message = "Declared main class is not accessible:" + e.getMessage();
            throw new InvalidJarFileException(message, e);
        }
    }

    public ClassLoader getClassLoader() {

        if (jcl != null)
            return jcl;

        validatePathIsReadable();

        jcl = new JarClassLoader();
        jcl.add(path.toString());

        jcl.getLocalLoader().setEnabled(true);
        jcl.getOsgiBootLoader().setEnabled(true);
        jcl.getParentLoader().setEnabled(true);
        jcl.getSystemLoader().setEnabled(true);
        jcl.getThreadLoader().setEnabled(true);
        jcl.getCurrentLoader().setEnabled(true);

        return jcl;
    }

    public void setPath(Path path) {
        if (path.isAbsolute())
            this.path = path;
        else
            this.path = jarPathServices.getJarPath().resolve(path);
        validatePathIsReadable();
    }
}
