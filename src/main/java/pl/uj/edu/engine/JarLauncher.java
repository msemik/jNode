package pl.uj.edu.engine;

import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;

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

public class JarLauncher {
    private Path path;
    private String mainClass;
    private JarClassLoader jcl;

    public JarLauncher(Path pathToJar) {
        this.path = pathToJar;
        validatePathIsReadable();
        mainClass = getMainClass(pathToJar);
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

            throw new IllegalArgumentException("Jar " + pathToJar + " doesn't contain Main-Class attribute");

        } catch (IOException e) {
            throw new RuntimeException(e);
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
        jcl.getParentLoader().setEnabled(false);
        jcl.getSystemLoader().setEnabled(false);
        jcl.getThreadLoader().setEnabled(false);
        jcl.getCurrentLoader().setEnabled(false);
        return jcl;
    }

    public void launchMain() {
        try {
            ClassLoader classLoader = getClassLoader();
            Class<?> mainClass = classLoader.loadClass(this.mainClass);

            Method main = mainClass.getMethod("main", String[].class);
            String[] args = new String[0];
            main.invoke(null, new Object[]{args});
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void validatePathIsReadable() {
        if (Files.notExists(path) || !Files.isReadable(path))
            throw new IllegalStateException("Unreadable path :" + path);
    }

}