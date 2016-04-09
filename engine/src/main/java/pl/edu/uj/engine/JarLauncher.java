package pl.edu.uj.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xeustechnologies.jcl.JarClassLoader;
import pl.edu.uj.jarpath.Jar;
import pl.edu.uj.jarpath.JarPathServices;

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
    private JarClassLoader jcl;

    public JarLauncher() {
    }

    public Object launchMain() {
        try {
            ClassLoader classLoader = getClassLoader();
            String mainClassName = getMainClass(jar);
            Class<?> mainClass = classLoader.loadClass(mainClassName);

            Method main = mainClass.getMethod("main", String[].class);
            String[] args = new String[0];
            return main.invoke(null, new Object[]{args});
        } catch (ClassNotFoundException e) {
            String message = "Declared main class doesn't exist:" + e.getMessage();
            throw new InvalidJarFileException(message, e);
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

    public ClassLoader getClassLoader() {
        if (jcl != null)
            return jcl;

        jcl = new JarClassLoader();
        jcl.add(jar.getAbsolutePathAsString());

        jcl.getLocalLoader().setEnabled(true);
        jcl.getOsgiBootLoader().setEnabled(true);
        jcl.getParentLoader().setEnabled(true);
        jcl.getSystemLoader().setEnabled(true);
        jcl.getThreadLoader().setEnabled(true);
        jcl.getCurrentLoader().setEnabled(true);

        return jcl;
    }

    private String getMainClass(Jar jar) {
        try {
            JarFile jarFile = new JarFile(jar.getAbsolutePathAsString());
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

    public void setJar(Jar jar) {
        this.jar = jar;
    }
}
