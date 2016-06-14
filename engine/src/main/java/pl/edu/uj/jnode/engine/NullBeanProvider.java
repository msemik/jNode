package pl.edu.uj.jnode.engine;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;

import static java.util.Optional.empty;

@Component
public class NullBeanProvider implements BeanProvider
{
    @Override public Optional<Object> getBean(Class<?> cls, Path jarName)
    {
        return empty();
    }
}
