package pl.edu.uj.jnode.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.jarpath.*;

import java.util.*;

@Component
public class JarContextRegistry
{
    @Autowired
    private ApplicationContext applicationContext;
    private Map<Jar, JarContext> jarJarContextMap = new HashMap<>();

    @EventListener
    public void on(NewJarCreatedEvent event)
    {
        Jar jar = event.getJar();
        createContext(jar);
    }

    public JarContext get(Jar jar)
    {
        JarContext jarContext = jarJarContextMap.get(jar);
        if(jarContext != null)
        {
            return jarContext;
        }
        return createContext(jar);
    }

    private JarContext createContext(Jar jar)
    {
        JarContext jarContext;
        jarContext = applicationContext.getBean(JarContext.class, jar);
        jarJarContextMap.put(jar, jarContext);
        return jarContext;
    }

    public boolean hasFor(Jar jar)
    {
        return jarJarContextMap.containsKey(jar);
    }
}
