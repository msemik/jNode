package pl.edu.uj.crosscuting.classloader;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import pl.edu.uj.crosscuting.Resources;
import pl.edu.uj.jnode.crosscuting.classloader.SomeClass;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ChildOnlyJarClassLoaderTest
{
    public static final String EXEMPLARY_CLASS_IN_BOTH_LOADERS = SomeClass.class.getCanonicalName();
    public static final String EXEMPLARY_CLASS_IN_CHILD_LOADER = "pl.test.example.SimpleContextClass";
    public static final String EXEMPLARY_CLASS_IN_PARENT_LOADER = ChildOnlyJarClassLoaderTest.class.getCanonicalName();
    public static final String EXEMPLARY_CLASS_IN_BOOTSTRAP_LOADER = String.class.getCanonicalName();
    public static final String EXEMPLARY_PARENT_DEPENDENCY_CLASS = MockitoJUnitRunner.class.getCanonicalName();
    public static final String ANOTHER_EXEMPLARY_CLASS_IN_CHILD_LOADER = "pl.test.example.ExemplaryMain";

    private Resources resources = Resources.getInstance();
    private ChildOnlyJarClassLoader classLoader;
    private String pathToSomeJar;
    private ChildFirstJarClassLoader childFirstJarClassLoader;

    @Before
    public void setUp() throws Exception
    {
        pathToSomeJar = resources.getPathAsString("somejar.jar");
        childFirstJarClassLoader = new ChildFirstJarClassLoader(pathToSomeJar);
        classLoader = childFirstJarClassLoader.getChildOnlyJarClassLoader();
    }

    @Test(expected = ClassNotFoundException.class)
    public void cantLoadClassNotFromJar() throws Exception
    {
        classLoader.loadClass(EXEMPLARY_CLASS_IN_PARENT_LOADER);
    }

    @Test
    public void canLoadBootstrapClass() throws Exception
    {
        classLoader.loadClass(EXEMPLARY_CLASS_IN_BOOTSTRAP_LOADER);
    }

    @Test(expected = ClassNotFoundException.class)
    public void cantLoadTestLibraries() throws Exception
    {
        classLoader.loadClass(EXEMPLARY_PARENT_DEPENDENCY_CLASS);
    }

    @Test
    public void canLoadClassFromJar() throws Exception
    {
        Class<?> aClass = classLoader.loadClass(EXEMPLARY_CLASS_IN_CHILD_LOADER);
        assertThat(aClass, notNullValue());
    }

    @Test
    public void canScanResources() throws Exception
    {
        PathMatchingResourcePatternResolver resolver = new ExtendedPathMatchingResourcePatternResolver(classLoader);
        Resource[] resources = resolver.getResources("file:/home/michal/jNode/crosscuting/src/test/resources/somejar.jar");
        System.out.println(resources.length);
        System.out.println(Arrays.deepToString(resources));
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.setResourceLoader(resolver);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
        Set<BeanDefinition> candidateComponents = provider.findCandidateComponents("pl");

        for(BeanDefinition bd : candidateComponents)
        {
            System.out.println(bd.toString());
        }
        List<String> classes = getCanonicalClassNames(candidateComponents);
        System.out.println("Size:" + candidateComponents.size());
        assertThat(candidateComponents.size(), greaterThan(0));
        assertThat(classes, hasItem(ANOTHER_EXEMPLARY_CLASS_IN_CHILD_LOADER));
        assertThat(classes, hasItem(EXEMPLARY_CLASS_IN_CHILD_LOADER));

    }

    private List<String> getCanonicalClassNames(Set<BeanDefinition> beanDefinitions)
    {
        return beanDefinitions.stream().map(BeanDefinition::getBeanClassName).collect(Collectors.toList());
    }
}
