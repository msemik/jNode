package pl.edu.uj.jnode.crosscuting.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

public class ChildOnlyJarClassLoader extends ClassLoader {
    private URLClassLoader urlClassLoader;

    public ChildOnlyJarClassLoader(String pathToJar) {
        this(Paths.get(pathToJar));
    }

    public ChildOnlyJarClassLoader(Path pathToJar) {
        URL[] urls;
        try {
            urls = new URL[]{new URL("jar:file:" + pathToJar.toString() + "!/")};
            System.out.println(urls[0]);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        urlClassLoader = new URLClassLoader(urls, null);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return urlClassLoader.loadClass(name);
    }

    @Override
    public URL getResource(String name) {
        return urlClassLoader.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return urlClassLoader.getResources(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return urlClassLoader.getResourceAsStream(name);
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        urlClassLoader.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        urlClassLoader.setPackageAssertionStatus(packageName, enabled);
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        urlClassLoader.setClassAssertionStatus(className, enabled);
    }

    @Override
    public void clearAssertionStatus() {
        urlClassLoader.clearAssertionStatus();
    }
}
