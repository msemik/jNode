package pl.edu.uj.context;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import pl.edu.uj.jarpath.Jar;
import pl.uj.edu.userlib.Callback;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

@Component
@Scope("prototype")
public class JarContext
{
    private final Logger logger = LoggerFactory.getLogger(JarContext.class);
    private final List<Object> beans = new ArrayList<>();
    private final List<Class<Annotation>> contextInjectors = new ArrayList<>();
    private final Optional<Class<Annotation>> contextAnnotation;

    public JarContext(Jar jar)
    {
        contextAnnotation = jar.getAnnotation(Context.class.getCanonicalName());
        Class<InjectContext> injectContextClass = InjectContext.class;
        jar.getAnnotation(injectContextClass.getCanonicalName()).ifPresent(contextInjectors::add);
        jar.getAnnotation(Autowired.class.getCanonicalName()).ifPresent(contextInjectors::add);

        if(contextInjectors.isEmpty())
        {
            logger.info("No context annotations found on class path");
            return;
        }

        if(!contextAnnotation.isPresent())
        {
            logger.warn("No context annotation found on classpath");
            return;
        }

        List<Class<?>> classes = findClassesWithAnyOfAnnotations(jar, contextInjectors);
        for(Class<?> cls : classes)
        {
            if(!ClassUtils.hasConstructor(cls))
            {
                logger.warn(cls.getCanonicalName() + " is declared as context bean, but doesn't provide default constructor");
                continue;
            }
            try
            {
                Object bean = cls.newInstance();
                beans.add(bean);
            }
            catch(InstantiationException | IllegalAccessException e)
            {
                logger.warn("Couldn't create bean for " + cls.getSimpleName());
                e.printStackTrace();
            }
        }
    }

    private List<Class<?>> findClassesWithAnyOfAnnotations(Jar jar, List<Class<Annotation>> annotations)
    {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader(jar.getClassLoader());
        provider.setResourceLoader(resourceLoader);
        // Filter to include only classes that have a particular annotation.

        for(Class<Annotation> annotation : annotations)
        {
            provider.addIncludeFilter(new AnnotationTypeFilter(annotation));
        }

        // Find classes in the given package (or subpackages)
        Set<BeanDefinition> beans = provider.findCandidateComponents("");
        List<Class<?>> classes = new ArrayList<>();
        for(BeanDefinition bd : beans)
        {
            // The BeanDefinition class gives access to the Class<?> and other attributes.
            Class<?> cls = jar.getClass(bd.getBeanClassName());
            classes.add(cls);
        }
        return classes;
    }

    public void autowire(Callback callback)
    {
        for(Class<Annotation> contextInjector : contextInjectors)
        {
            List<Field> fieldsToInjectContext = FieldUtils.getFieldsListWithAnnotation(callback.getClass(), contextInjector);
            for(Field field : fieldsToInjectContext)
            {
                boolean accessible = field.isAccessible();
                if(!accessible)
                {
                    field.setAccessible(true);
                }

                try
                {
                    Object fieldValue = field.get(callback);
                    if(fieldValue == null)
                    {
                        Object autowiredBean = findBean(field.getType());
                        field.set(callback, autowiredBean);
                        logger.debug("Successfully autowired field '" + field.getName() + "' with " + autowiredBean);
                    }
                    else
                    {
                        logger.warn("No suitable bean found for '" + field.getName() + "' in " + callback);
                    }
                }
                catch(IllegalAccessException e)
                {
                    //shouldn't happen as field was set accessible
                    e.printStackTrace();
                }

                if(!accessible)
                {
                    field.setAccessible(false);
                }
            }
        }
    }

    private Object findBean(Class<?> type)
    {
        for(Object bean : beans)
        {
            if(type.isInstance(bean))
            {
                return bean;
            }
        }
        logger.error("No bean found for type:" + type.getCanonicalName());
        return null;
    }
}
