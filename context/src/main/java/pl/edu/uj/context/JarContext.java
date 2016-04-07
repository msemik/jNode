package pl.edu.uj.context;

import org.apache.commons.lang3.*;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.*;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.validation.ValidationUtils;
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
    private final Class<Annotation> autowiredAnnotation;
    private final Class<Annotation> contextBeanAnnotation;

    public JarContext(Jar jar)
    {
        contextBeanAnnotation = jar.getAnnotation(Context.class.getCanonicalName());
        autowiredAnnotation = jar.getAnnotation(Autowired.class.getCanonicalName());
        if(contextBeanAnnotation == null || autowiredAnnotation == null){
            logger.info("No context annotations found on class path");
            return;
        }

        List<Class<?>> classes = findClassesWithAnnotation(jar, contextBeanAnnotation);
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

    private List<Class<?>> findClassesWithAnnotation(Jar jar, Class<Annotation> expectedAnnotation)
    {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader(jar.getClassLoader());
        provider.setResourceLoader(resourceLoader);
        // Filter to include only classes that have a particular annotation.
        provider.addIncludeFilter(new AnnotationTypeFilter(expectedAnnotation));
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
        for(Field field : FieldUtils.getFieldsListWithAnnotation(callback.getClass(), autowiredAnnotation))
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
