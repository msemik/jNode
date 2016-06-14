package pl.edu.uj.jnode.engine;

import java.nio.file.Path;
import java.util.Optional;

public interface BeanProvider
{
    Optional<Object> getBean(Class<?> cls, Path jarName);
}
