package pl.edu.uj.crosscuting.classloader;

import sun.misc.CompoundEnumeration;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;

public class ChildFirstJarClassLoader extends ClassLoader
{
    private final ClassLoader parent;
    private ChildOnlyJarClassLoader childOnlyJarClassLoader;

    public ChildFirstJarClassLoader(String pathToJar, ClassLoader parent)
    {
        super(null);
        this.parent = parent;
        this.childOnlyJarClassLoader = new ChildOnlyJarClassLoader(pathToJar);
    }

    public ChildFirstJarClassLoader(Path pathToJar, ClassLoader parent)
    {
        super(null);
        this.parent = parent;
        this.childOnlyJarClassLoader = new ChildOnlyJarClassLoader(pathToJar);
    }

    public ChildFirstJarClassLoader(ChildOnlyJarClassLoader childOnlyClassLoader, ClassLoader parent)
    {
        super(null);
        this.parent = parent;
        this.childOnlyJarClassLoader = childOnlyClassLoader;
    }

    public ChildFirstJarClassLoader(ChildOnlyJarClassLoader childOnlyClassLoader)
    {
        this(childOnlyClassLoader, ChildFirstJarClassLoader.class.getClassLoader());
    }

    public ChildFirstJarClassLoader(String pathToJar)
    {
        this(new ChildOnlyJarClassLoader(pathToJar), ChildFirstJarClassLoader.class.getClassLoader());
    }

    public ChildFirstJarClassLoader(Path pathToJar)
    {
        this(pathToJar.toString());
    }

    @Override public Class<?> loadClass(String name) throws ClassNotFoundException
    {
        try
        {
            return childOnlyJarClassLoader.loadClass(name);
        }
        catch(ClassNotFoundException e)
        {
            return parent.loadClass(name);
        }
    }

    @Override public URL getResource(String name)
    {
        URL resource = childOnlyJarClassLoader.getResource(name);
        if(resource != null)
            return resource;
        return parent.getResource(name);
    }

    @Override public Enumeration<URL> getResources(String name) throws IOException
    {
        Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[2];
        tmp[0] = childOnlyJarClassLoader.getResources(name);
        tmp[1] = parent.getResources(name);

        return new CompoundEnumeration<>(tmp);
    }

    @Override public InputStream getResourceAsStream(String name)
    {
        InputStream resourceAsStream = childOnlyJarClassLoader.getResourceAsStream(name);
        if(resourceAsStream != null)
            return resourceAsStream;
        return parent.getResourceAsStream(name);
    }

    @Override public void setDefaultAssertionStatus(boolean enabled)
    {
        childOnlyJarClassLoader.setDefaultAssertionStatus(enabled);
        parent.setDefaultAssertionStatus(enabled);
    }

    @Override public void setPackageAssertionStatus(String packageName, boolean enabled)
    {
        childOnlyJarClassLoader.setPackageAssertionStatus(packageName, enabled);
        parent.setPackageAssertionStatus(packageName, enabled);
    }

    @Override public void setClassAssertionStatus(String className, boolean enabled)
    {
        childOnlyJarClassLoader.setClassAssertionStatus(className, enabled);
        parent.setClassAssertionStatus(className, enabled);
    }

    @Override public void clearAssertionStatus()
    {
        childOnlyJarClassLoader.clearAssertionStatus();
        parent.clearAssertionStatus();
    }

    public ChildOnlyJarClassLoader getChildOnlyJarClassLoader()
    {
        return childOnlyJarClassLoader;
    }
}
