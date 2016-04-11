package pl.edu.uj.jnode.crosscutting.classloader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import pl.edu.uj.jnode.crosscuting.Resources;
import pl.edu.uj.jnode.crosscuting.classloader.ChildFirstJarClassLoader;
import pl.edu.uj.jnode.crosscuting.classloader.ChildOnlyJarClassLoader;
import pl.edu.uj.jnode.crosscuting.classloader.ExtendedPathMatchingResourcePatternResolver;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ChildFirstJarClassLoaderTest {
    public static final String EXEMPLARY_CLASS_IN_BOTH_LOADERS = SomeClass.class.getCanonicalName();
    public static final String EXEMPLARY_CLASS_IN_CHILD_LOADER = "pl.test.example.SimpleContextClass";
    public static final String EXEMPLARY_CLASS_IN_PARENT_LOADER = ChildFirstJarClassLoaderTest.class.getCanonicalName();
    private Resources resources = Resources.getInstance();
    private ChildFirstJarClassLoader classLoader;
    private String pathToJar;
    private ClassLoader parent;
    private ChildOnlyJarClassLoader child;

    @Before
    public void setUp() throws Exception {
        pathToJar = resources.getPathAsString("somejar.jar");
    }

    @Test
    public void canLoadChildClass() throws Exception {
        classLoader = new ChildFirstJarClassLoader(pathToJar);
        Class<?> aClass = classLoader.loadClass(EXEMPLARY_CLASS_IN_CHILD_LOADER);
        assertThat(aClass, notNullValue());

    }

    @Test
    public void canLoadParentClass() throws Exception {
        classLoader = new ChildFirstJarClassLoader(pathToJar);
        Class<?> aClass = classLoader.loadClass(EXEMPLARY_CLASS_IN_PARENT_LOADER);
        assertThat(aClass, notNullValue());
    }

    @Test
    public void whenBothLoadersHasClassThenLoadsFromChild() throws Exception {
        parent = this.getClass().getClassLoader();
        classLoader = new ChildFirstJarClassLoader(pathToJar, parent);
        child = classLoader.getChildOnlyJarClassLoader();

        Class<?> clsInParent = parent.loadClass(EXEMPLARY_CLASS_IN_BOTH_LOADERS);

        Class<?> clsInChildFirst = classLoader.loadClass(EXEMPLARY_CLASS_IN_BOTH_LOADERS);
        Class<?> clsInChild = child.loadClass(EXEMPLARY_CLASS_IN_BOTH_LOADERS);

        assertThat(clsInChildFirst, equalTo(clsInChild));
        assertThat(clsInChildFirst, not(equalTo(clsInParent)));

    }

    @Test
    public void canFindResources() {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);
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
        assertThat(classes, hasItem(EXEMPLARY_CLASS_IN_BOTH_LOADERS));
        assertThat(classes, hasItem(EXEMPLARY_CLASS_IN_CHILD_LOADER));
        assertThat(classes, hasItem(EXEMPLARY_CLASS_IN_PARENT_LOADER));

    }

    @Test
    public void whenGetClassLoaderFromChildThenItHasAccessToClasses() throws Exception {
        try {
            classLoader = new ChildFirstJarClassLoader(pathToJar);
            Class<?> mainClass = classLoader.loadClass(EXEMPLARY_CLASS_IN_BOTH_LOADERS);
            ClassLoader classLoaderOfMainClass = mainClass.getClassLoader();
            classLoaderOfMainClass.loadClass(EXEMPLARY_CLASS_IN_PARENT_LOADER);
            System.out.println("loading1 " + EXEMPLARY_CLASS_IN_CHILD_LOADER);
            classLoader.loadClass(EXEMPLARY_CLASS_IN_CHILD_LOADER);
            System.out.println("loading2 " + EXEMPLARY_CLASS_IN_CHILD_LOADER);
            classLoaderOfMainClass.loadClass(EXEMPLARY_CLASS_IN_CHILD_LOADER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void encodeVariousFilePaths() throws Exception {
        assertCreateCorrectUrl("file.jar", "jar:file:file.jar!/");
        assertCreateCorrectUrl(" f ile .jar", "jar:file: f ile .jar!/");
        assertCreateCorrectUrl("_f-i/le.jar", "jar:file:_f-i/le.jar!/");
        assertCreateCorrectUrl("%20Asd%3A.jar", "jar:file: Asd:.jar!/");
        assertCreateCorrectUrl("%3A%2F%2Fmywebsite%2Fdocs%2Fenglish%2Fsite%2Fmybook.do%3Frequest_type",
                "jar:file:://mywebsite/docs/english/site/mybook.do?request_type!/");
    }

    private void assertCreateCorrectUrl(String pathToJar, String expectedUrlToJar) {
        URL[] encodedUrls = ChildFirstJarClassLoader.pathToUrls(pathToJar);
        assertThat(encodedUrls.length, equalTo(1));
        String firstUrl = encodedUrls[0].toString();
        assertThat(firstUrl, equalTo(expectedUrlToJar));
    }

    private List<String> getCanonicalClassNames(Set<BeanDefinition> beanDefinitions) {
        return beanDefinitions.stream().map(BeanDefinition::getBeanClassName).collect(Collectors.toList());
    }
}