package pl.edu.uj.crosscuting.classloader;

import sun.misc.CompoundEnumeration;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

public class ChildFirstJarClassLoader extends URLClassLoader
{
    private final ClassLoader parent;
    private final ChildOnlyJarClassLoader childOnlyJarClassLoader;
    private Mode mode = Mode.CHILD_FIRST;

    public ChildFirstJarClassLoader(String pathToJar)
    {
        super(pathToUrls(pathToJar), null);
        this.parent = this.getClass().getClassLoader();
        this.childOnlyJarClassLoader = new ChildOnlyJarClassLoader(this);
    }

    public ChildFirstJarClassLoader(String pathToJar, ClassLoader parent)
    {
        super(pathToUrls(pathToJar), null);
        this.parent = parent;
        this.childOnlyJarClassLoader = new ChildOnlyJarClassLoader(this);
    }

    protected static URL[] pathToUrls(String pathToJar)
    {
        try
        {
            pathToJar = URLEncoder.encode(pathToJar, "UTF-8").replace("+", "%20");
            return new URL[] { new URL("jar:file:" + pathToJar + "!/") };
        }
        catch(MalformedURLException | UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected Mode getMode()
    {
        return mode;
    }

    protected void setMode(Mode mode)
    {
        this.mode = mode;
    }

    @Override public Class<?> loadClass(String name) throws ClassNotFoundException
    {
        try
        {
            return super.loadClass(name);
        }
        catch(ClassNotFoundException e)
        {
            if(!canCallParent())
            {
                throw e;
            }
            return parent.loadClass(name);
        }
    }

    @Override public URL getResource(String name)
    {
        URL resource = super.getResource(name);
        if(resource != null || !canCallParent())
        {
            return resource;
        }
        return parent.getResource(name);
    }

    @Override public Enumeration<URL> getResources(String name) throws IOException
    {
        if(!canCallParent())
        {
            return super.getResources(name);
        }

        Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[2];
        tmp[0] = super.getResources(name);
        tmp[1] = parent.getResources(name);

        return new CompoundEnumeration<>(tmp);

    }

    @Override public InputStream getResourceAsStream(String name)
    {
        InputStream resourceAsStream = super.getResourceAsStream(name);
        if(resourceAsStream != null || !canCallParent())
        {
            return resourceAsStream;
        }
        return parent.getResourceAsStream(name);
    }

    @Override public void setDefaultAssertionStatus(boolean enabled)
    {
        super.setDefaultAssertionStatus(enabled);
        if(canCallParent())
        {
            parent.setDefaultAssertionStatus(enabled);
        }
    }

    @Override public void setPackageAssertionStatus(String packageName, boolean enabled)
    {
        super.setPackageAssertionStatus(packageName, enabled);
        if(canCallParent())
        {
            parent.setPackageAssertionStatus(packageName, enabled);
        }
    }

    @Override public void setClassAssertionStatus(String className, boolean enabled)
    {
        super.setClassAssertionStatus(className, enabled);
        if(canCallParent())
        {
            parent.setClassAssertionStatus(className, enabled);
        }
    }

    @Override public void clearAssertionStatus()
    {
        super.clearAssertionStatus();
        if(canCallParent())
        {
            parent.clearAssertionStatus();
        }
    }

    private boolean canCallParent()
    {
        return parent != null && mode != Mode.CHILD_ONLY;
    }

    public ChildOnlyJarClassLoader getChildOnlyJarClassLoader()
    {
        return childOnlyJarClassLoader;
    }

    protected enum Mode
    {
        CHILD_FIRST, CHILD_ONLY
    }
}
