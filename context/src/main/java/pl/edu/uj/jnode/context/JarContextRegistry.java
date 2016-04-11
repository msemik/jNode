package pl.edu.uj.jnode.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.jarpath.NewJarCreatedEvent;

import java.util.HashMap;
import java.util.Map;

@Component
public class JarContextRegistry {
    @Autowired
    private ApplicationContext applicationContext;
    private Map<Jar, JarContext> jarJarContextMap = new HashMap<>();

    @EventListener
    public void on(NewJarCreatedEvent event) {
        Jar jar = event.getJar();
        createContext(jar);
    }

    private JarContext createContext(Jar jar) {
        JarContext jarContext;
        jarContext = applicationContext.getBean(JarContext.class, jar);
        jarJarContextMap.put(jar, jarContext);
        return jarContext;
    }

    public JarContext get(Jar jar) {
        JarContext jarContext = jarJarContextMap.get(jar);
        if (jarContext != null) {
            return jarContext;
        }
        return createContext(jar);
    }

    public boolean hasFor(Jar jar) {
        return jarJarContextMap.containsKey(jar);
    }
}
