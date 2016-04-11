package pl.edu.uj.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.edu.uj.crosscuting.classloader.ChildFirstJarClassLoader;
import pl.edu.uj.jarpath.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.Iterator;
import java.util.jar.*;

/**
 * Created by michal on 31.10.15.
 */

@Component
@Scope("prototype")
public class JarLauncher
{
    @Autowired
    private JarPathServices jarPathServices;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    private Jar jar;
    private ChildFirstJarClassLoader childFirstJarClassLoader;

    public JarLauncher()
    {
    }

    public Object launchMain()
    {
        Class<?> mainClass = loadMainClass();
        try
        {
            Method main = mainClass.getMethod("main", String[].class);
            String[] args = new String[0];
            return main.invoke(null, new Object[] { args });
        }
        catch(InvocationTargetException e)
        {
            throw new UserApplicationException(e.getCause());
        }
        catch(NoSuchMethodException e)
        {
            String message = "Declared main class doesn't have proper main method:" + e.getMessage();
            throw new InvalidJarFileException(message, e);
        }
        catch(IllegalAccessException e)
        {
            String message = "Declared main class is not accessible:" + e.getMessage();
            throw new InvalidJarFileException(message, e);
        }
    }

    private Class<?> loadMainClass()
    {
        try
        {
            ClassLoader classLoader = getClassLoader();
            String mainClassName = getMainClass(jar);
            return classLoader.loadClass(mainClassName);
        }
        catch(ClassNotFoundException e)
        {
            String message = "Declared main class doesn't exist:" + e.getMessage();
            throw new InvalidJarFileException(message, e);
        }
    }

    public ClassLoader getClassLoader()
    {
        return getChildFirstClassLoader();
    }

    public ClassLoader getJarOnlyClassLoader()
    {
        return getChildFirstClassLoader().getChildOnlyJarClassLoader();
    }

    private ChildFirstJarClassLoader getChildFirstClassLoader()
    {
        if(childFirstJarClassLoader != null)
            return childFirstJarClassLoader;
        childFirstJarClassLoader = new ChildFirstJarClassLoader(jar.getAbsolutePathAsString());
        return childFirstJarClassLoader;
    }

    private String getMainClass(Jar jar)
    {
        try
        {
            JarFile jarFile = new JarFile(jar.getAbsolutePathAsString());
            Manifest manifest = jarFile.getManifest();
            Attributes mainAttributes = manifest.getMainAttributes();

            for(Iterator it = mainAttributes.keySet().iterator(); it.hasNext(); )
            {
                Attributes.Name attribute = (Attributes.Name) it.next();
                if(attribute.toString().equals("Main-Class"))
                    return (String) mainAttributes.get(attribute);
            }

            throw new InvalidJarFileException("Jar doesn't contain Main-Class attribute");

        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setJar(Jar jar)
    {
        this.jar = jar;
    }
}
