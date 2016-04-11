package pl.edu.uj.jnode.crosscuting.classloader;

import pl.edu.uj.jnode.crosscuting.classloader.ChildFirstJarClassLoader.Mode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import static pl.edu.uj.jnode.crosscuting.classloader.ChildFirstJarClassLoader.Mode.CHILD_ONLY;

public class ChildOnlyJarClassLoader extends ClassLoader {
    private ChildFirstJarClassLoader jarClassLoader;
    private Mode previousMode;

    protected ChildOnlyJarClassLoader(ChildFirstJarClassLoader jarClassLoader) {
        this.jarClassLoader = jarClassLoader;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        setToChildOnlyMode();
        Class<?> cls = jarClassLoader.loadClass(name);
        resetModeToPrevious();
        return cls;
    }

    private void resetModeToPrevious() {
        jarClassLoader.setMode(previousMode);
    }

    private void setToChildOnlyMode() {
        previousMode = jarClassLoader.getMode();
        jarClassLoader.setMode(CHILD_ONLY);
    }

    @Override
    public URL getResource(String name) {
        setToChildOnlyMode();
        URL resource = jarClassLoader.getResource(name);
        resetModeToPrevious();
        return resource;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        setToChildOnlyMode();
        Enumeration<URL> resources = jarClassLoader.getResources(name);
        resetModeToPrevious();
        return resources;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        setToChildOnlyMode();
        InputStream resourceAsStream = jarClassLoader.getResourceAsStream(name);
        resetModeToPrevious();
        return resourceAsStream;
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        setToChildOnlyMode();
        jarClassLoader.setDefaultAssertionStatus(enabled);
        resetModeToPrevious();
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        setToChildOnlyMode();
        jarClassLoader.setPackageAssertionStatus(packageName, enabled);
        resetModeToPrevious();
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        setToChildOnlyMode();
        jarClassLoader.setClassAssertionStatus(className, enabled);
        resetModeToPrevious();
    }

    @Override
    public void clearAssertionStatus() {
        setToChildOnlyMode();
        jarClassLoader.clearAssertionStatus();
        resetModeToPrevious();
    }
}
