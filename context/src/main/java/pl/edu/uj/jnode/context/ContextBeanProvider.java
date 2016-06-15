package pl.edu.uj.jnode.context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.engine.BeanProvider;
import pl.edu.uj.jnode.jarpath.JarFactory;

import java.nio.file.Path;
import java.util.Optional;

@Component
@Primary
public class ContextBeanProvider implements BeanProvider
{
    @Autowired
    private JarContextRegistry jarContextRegistry;
    @Autowired(required = false)
    private JarFactory jarFactory;

    @Override public Optional<Object> getBean(Class<?> cls, Path jarName)
    {
        return jarContextRegistry.get(jarFactory.getFor(jarName)).getBean(cls);
    }
}
