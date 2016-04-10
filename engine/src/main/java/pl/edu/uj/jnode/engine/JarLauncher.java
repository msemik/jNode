package pl.edu.uj.jnode.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.crosscuting.classloader.ChildFirstJarClassLoader;
import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.jarpath.JarPathServices;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    @Autowired
    private JarPathServices jarPathServices;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    private Jar jar;
    private ChildFirstJarClassLoader childFirstJarClassLoader;

    public JarLauncher() {
    }

    public Object launchMain() {
        Class<?> mainClass = loadMainClass();
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

    private Class<?> loadMainClass() {
        try {
            ClassLoader classLoader = getClassLoader();
            String mainClassName = getMainClass(jar);
            return classLoader.loadClass(mainClassName);
        } catch (ClassNotFoundException e) {
            String message = "Declared main class doesn't exist:" + e.getMessage();
            throw new InvalidJarFileException(message, e);
        }
    }

    public ClassLoader getClassLoader() {
        return getChildFirstClassLoader();
    }

    private String getMainClass(Jar jar) {
        try {
            JarFile jarFile = new JarFile(jar.getAbsolutePathAsString());
            Manifest manifest = jarFile.getManifest();
            Attributes mainAttributes = manifest.getMainAttributes();

            for (Iterator it = mainAttributes.keySet().iterator(); it.hasNext(); ) {
                Attributes.Name attribute = (Attributes.Name) it.next();
                if (attribute.toString().equals("Main-Class")) {
                    return (String) mainAttributes.get(attribute);
                }
            }

            throw new InvalidJarFileException("Jar doesn't contain Main-Class attribute");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ChildFirstJarClassLoader getChildFirstClassLoader() {
        if (childFirstJarClassLoader != null) {
            return childFirstJarClassLoader;
        }
        ClassLoader parentClassLoader = this.getClass().getClassLoader();
        childFirstJarClassLoader = new ChildFirstJarClassLoader(jar.getAbsolutePathAsString(), parentClassLoader);
        return childFirstJarClassLoader;
    }

    public ClassLoader getJarOnlyClassLoader() {
        return getChildFirstClassLoader().getChildOnlyJarClassLoader();
    }

    public void setJar(Jar jar) {
        this.jar = jar;
    }
}
