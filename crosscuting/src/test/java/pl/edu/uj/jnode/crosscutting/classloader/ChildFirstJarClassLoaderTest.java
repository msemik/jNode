package pl.edu.uj.jnode.crosscutting.classloader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import pl.edu.uj.jnode.crosscuting.classloader.ChildFirstJarClassLoader;
import pl.edu.uj.jnode.crosscuting.classloader.ChildOnlyJarClassLoader;
import pl.edu.uj.jnode.crosscuting.classloader.ExtendedPathMatchingResourcePatternResolver;
import pl.edu.uj.jnode.crosscutting.Resources;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ChildFirstJarClassLoaderTest {
    public static final String EXEMPLARY_CLASS = "pl.test.example.ExemplaryMain";
    public static final String ANOTHER_EXEMPLARY_CLASS = "pl.test.example.SimpleContextClass";
    private Resources resources = Resources.getInstance();
    private ChildFirstJarClassLoader classLoader;
    private String pathToJar;

    @Before

    public void setUp() throws Exception {
        pathToJar = resources.getPathAsString("somejar.jar");
    }

    @Test
    public void canLoadChildClass() throws Exception {
        classLoader = new ChildFirstJarClassLoader(pathToJar);
        Class<?> aClass = classLoader.loadClass(EXEMPLARY_CLASS);
        assertThat(aClass, notNullValue());
    }

    @Test
    public void canLoadParentClass() throws Exception {
        classLoader = new ChildFirstJarClassLoader(pathToJar);
        Class<?> aClass = classLoader.loadClass(ChildFirstJarClassLoaderTest.class.getCanonicalName());
        assertThat(aClass, notNullValue());
    }

    @Test
    public void whenBothLoadersHasClassThenLoadsFromChild() throws Exception {
        ChildOnlyJarClassLoader parent = new ChildOnlyJarClassLoader(pathToJar);
        ChildOnlyJarClassLoader child = new ChildOnlyJarClassLoader(pathToJar);
        classLoader = new ChildFirstJarClassLoader(child, parent);

        Class<?> clsInParent = parent.loadClass(EXEMPLARY_CLASS);
        Class<?> clsInChild = child.loadClass(EXEMPLARY_CLASS);

        Class<?> clsInChildFirst = classLoader.loadClass(EXEMPLARY_CLASS);

        assertThat(clsInChildFirst, equalTo(clsInChild));
        assertThat(clsInChildFirst, not(equalTo(clsInParent)));
    }

    @Test
    public void canFindResources() {
        classLoader = new ChildFirstJarClassLoader(pathToJar);
        PathMatchingResourcePatternResolver resolver = new ExtendedPathMatchingResourcePatternResolver(classLoader);
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.setResourceLoader(resolver);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
        Set<BeanDefinition> candidateComponents = provider.findCandidateComponents("pl");

        for (BeanDefinition bd : candidateComponents) {
            System.out.println(bd.toString());
        }
        List<String> classes = getCanonicalClassNames(candidateComponents);
        System.out.println("Size:" + candidateComponents.size());
        assertThat(candidateComponents.size(), greaterThan(0));
        assertThat(classes, hasItem(EXEMPLARY_CLASS));
        assertThat(classes, hasItem(ANOTHER_EXEMPLARY_CLASS));
        assertThat(classes, hasItem(this.getClass().getCanonicalName()));
    }

    private List<String> getCanonicalClassNames(Set<BeanDefinition> beanDefinitions) {
        return beanDefinitions.stream().map(BeanDefinition::getBeanClassName).collect(Collectors.toList());
    }

    @Test
    public void whenGetClassLoaderFromChildThenItHasAccessToClasses() throws Exception {
        classLoader = new ChildFirstJarClassLoader(pathToJar);
        Class<?> mainClass = classLoader.loadClass(EXEMPLARY_CLASS);
        ClassLoader classLoaderOfMainClass = mainClass.getClassLoader();
        classLoaderOfMainClass.loadClass(ANOTHER_EXEMPLARY_CLASS);
        classLoaderOfMainClass.loadClass(ChildFirstJarClassLoaderTest.class.getCanonicalName());
    }
}