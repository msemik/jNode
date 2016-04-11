package pl.edu.uj.jnode.crosscutting.classloader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import pl.edu.uj.jnode.crosscuting.classloader.ChildOnlyJarClassLoader;
import pl.edu.uj.jnode.crosscuting.classloader.ExtendedPathMatchingResourcePatternResolver;
import pl.edu.uj.jnode.crosscutting.Resources;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ChildOnlyJarClassLoaderTest {
    private Resources resources = Resources.getInstance();
    private ChildOnlyJarClassLoader classLoader;
    private String pathToSomeJar;

    @Before
    public void setUp() throws Exception {
        pathToSomeJar = resources.getPathAsString("somejar.jar");
        classLoader = new ChildOnlyJarClassLoader(pathToSomeJar);
    }

    @Test(expected = ClassNotFoundException.class)
    public void cantLoadClassNotFromJar() throws Exception {
        classLoader.loadClass(ChildOnlyJarClassLoaderTest.class.getCanonicalName());
    }

    @Test
    public void canLoadBootstrapClass() throws Exception {
        classLoader.loadClass(String.class.getCanonicalName());
    }

    @Test(expected = ClassNotFoundException.class)
    public void cantLoadTestLibraries() throws Exception {
        classLoader.loadClass(MockitoJUnitRunner.class.getCanonicalName());
    }

    @Test
    public void canLoadClassFromJar() throws Exception {
        Class<?> aClass = classLoader.loadClass("pl.test.example.ExemplaryMain");
        assertThat(aClass, notNullValue());
    }

    @Test
    public void canScanResources() throws Exception {
        PathMatchingResourcePatternResolver resolver = new ExtendedPathMatchingResourcePatternResolver(classLoader);
        //resolver.setPathMatcher();
        Resource[] resources = resolver.getResources("file:/home/michal/jNode/crosscuting/src/test/resources/somejar.jar");
        System.out.println(resources.length);
        System.out.println(Arrays.deepToString(resources));
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
        assertThat(classes, hasItem("pl.test.example.ExemplaryMain"));
        assertThat(classes, hasItem("pl.test.example.SimpleContextClass"));
    }

    private List<String> getCanonicalClassNames(Set<BeanDefinition> beanDefinitions) {
        return beanDefinitions.stream().map(BeanDefinition::getBeanClassName).collect(Collectors.toList());
    }

    private void ASD() throws IOException {
        Enumeration<URL> en = classLoader.getResources("");
        if (en.hasMoreElements()) {
            URL url = en.nextElement();
            JarURLConnection urlcon = (JarURLConnection) (url.openConnection());
            try (JarFile jar = urlcon.getJarFile();) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement().getName();
                    System.out.println(entry);
                }
            }
        }
    }
}
