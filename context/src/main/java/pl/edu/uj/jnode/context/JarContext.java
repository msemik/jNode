package pl.edu.uj.jnode.context;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.userlib.Callback;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@Scope("prototype")
public class JarContext {
    private final Logger logger = LoggerFactory.getLogger(JarContext.class);
    private final List<Object> beans = new ArrayList<>();
    private final List<Class<Annotation>> contextInjectors = new ArrayList<>();
    private final Optional<Class<Annotation>> contextAnnotation;

    public JarContext(Jar jar) {
        contextAnnotation = jar.getAnnotation(Context.class.getCanonicalName());
        jar.getAnnotation(InjectContext.class.getCanonicalName()).ifPresent(contextInjectors::add);
        jar.getAnnotation(Autowired.class.getCanonicalName()).ifPresent(contextInjectors::add);

        if (contextInjectors.isEmpty()) {
            logger.info("No context annotations found on class path");
            return;
        }

        if (!contextAnnotation.isPresent()) {
            logger.warn("No context annotation found on classpath");
            return;
        }

        List<Class<?>> classes = findClassesWithAnnotation(jar, contextAnnotation.get());
        if (classes.isEmpty()) {
            logger.warn("No context beans found on class path");
        }
        for (Class<?> cls : classes) {
            logger.debug("Found context bean definition:" + cls.getCanonicalName());
            if (!ClassUtils.hasConstructor(cls)) {
                logger.warn(cls.getCanonicalName() + " is declared as context bean, but doesn't provide default constructor");
                continue;
            }
            try {
                Object bean = cls.newInstance();
                beans.add(bean);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.warn("Couldn't create bean for " + cls.getSimpleName());
                e.printStackTrace();
            }
        }
    }

    private List<Class<?>> findClassesWithAnnotation(Jar jar, Class<Annotation> contextAnnotation) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        ClassLoader classLoader = jar.getJarOnlyClassLoader();
        ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver(classLoader);
        provider.setResourceLoader(resourceLoader);
        // Filter to include only classes that have a particular annotation.
        provider.addIncludeFilter(new AnnotationTypeFilter(contextAnnotation));

        // Find classes in the given package (or subpackages)
        Set<BeanDefinition> beans = provider.findCandidateComponents("pl");
        List<Class<?>> classes = new ArrayList<>();
        for (BeanDefinition bd : beans) {
            // The BeanDefinition class gives access to the Class<?> and other attributes.
            Class<?> cls = jar.getClass(bd.getBeanClassName());
            classes.add(cls);
        }
        return classes;
    }

    public void injectContext(Callback callback) {
        for (Class<Annotation> contextInjector : contextInjectors) {
            List<Field> fieldsToInjectContext = FieldUtils.getFieldsListWithAnnotation(callback.getClass(), contextInjector);
            for (Field field : fieldsToInjectContext) {
                boolean accessible = field.isAccessible();
                if (!accessible) {
                    field.setAccessible(true);
                }

                try {
                    Object fieldValue = field.get(callback);
                    if (fieldValue == null) {
                        Object autowiredBean = findBean(field.getType());
                        field.set(callback, autowiredBean);
                        if (autowiredBean != null) {
                            logger.debug("Successfully injected field '" + field.getName() + "' with " + autowiredBean);
                        } else {
                            logger.warn("No suitable bean found for '" + field.getName() + "' in " + callback);
                        }
                    }
                } catch (IllegalAccessException e) {
                    //shouldn't happen as field was set accessible
                    e.printStackTrace();
                }

                if (!accessible) {
                    field.setAccessible(false);
                }
            }
        }
    }

    private Object findBean(Class<?> type) {
        for (Object bean : beans) {
            if (type.isInstance(bean)) {
                return bean;
            }
        }
        logger.error("No bean found for type:" + type.getCanonicalName());
        return null;
    }
}
