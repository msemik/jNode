package pl.edu.uj.jnode.crosscuting.classloader;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ExtendedPathMatchingResourcePatternResolver extends PathMatchingResourcePatternResolver {
    public ExtendedPathMatchingResourcePatternResolver(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    protected Resource[] findAllClassPathResources(String location) throws IOException {
        String path = location;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if ("".equals(path)) {
            Set<Resource> result = new LinkedHashSet<Resource>(16);
            result.addAll(Arrays.asList(super.findAllClassPathResources(location)));
            addAllClassLoaderJarUrls(getClassLoader(), result);
            return result.toArray(new Resource[result.size()]);
        }
        return super.findAllClassPathResources(location);
    }

    private void addAllClassLoaderJarUrls(ClassLoader classLoader, Set<Resource> result) {
        if (classLoader != null) {
            if (classLoader instanceof URLClassLoader) {
                addAllClassLoaderJarUrls(((URLClassLoader) classLoader).getURLs(), result);
            }
            addAllClassLoaderJarUrls(classLoader.getParent(), result);
        }
    }

    private void addAllClassLoaderJarUrls(URL[] urls, Set<Resource> result) {
        for (URL url : urls) {
            if ("file".equals(url.getProtocol()) && url.toString().toLowerCase().endsWith(".jar")) {
                try {
                    URL jarUrl = new URL("jar:" + url.toString() + "!/");
                    jarUrl.openConnection();
                    result.add(convertClassLoaderURL(jarUrl));
                } catch (Exception ex) {
                    throw new RuntimeException("Cannot search for matching files underneath " + url + " because it cannot be accessed as a JAR", ex);
                }
            }
        }
    }
}