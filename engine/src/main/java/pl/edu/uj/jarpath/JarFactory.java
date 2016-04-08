package pl.edu.uj.jarpath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.edu.uj.engine.NodeIdFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class JarFactory {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private JarPathServices jarPathServices;
    @Autowired
    private NodeIdFactory nodeIdFactory;
    private List<Jar> jars = new CopyOnWriteArrayList<>();

    public Jar getFor(Path pathToJar) {
        return findAndStoreIfAbsent(pathToJar);
    }

    private Jar findAndStoreIfAbsent(Path pathToJar) {
        if (pathToJar.isAbsolute()) {
            pathToJar = jarPathServices.getPathSinceJarPath(pathToJar);
        }
        pathToJar = pathToJar.normalize();
        Jar jar = find(pathToJar);
        if (jar == null) {
            String nodeId;
            if (isFileName(pathToJar)) {
                nodeId = nodeIdFactory.getCurrentNodeId();
            } else {
                nodeId = pathToJar.getParent().toString();
            }
            jar = applicationContext.getBean(Jar.class, nodeId, pathToJar);
            jar.validate();
            jars.add(jar);
        }
        return jar;
    }

    private Jar find(Path pathToJar) {
        return jars.stream().filter(jar -> jar.hasRelativePath(pathToJar)).findAny().orElse(null);
    }

    private boolean isFileName(Path pathToJar) {
        return pathToJar.getNameCount() == 1;
    }

    public Jar getFor(String pathToJar) {
        return findAndStoreIfAbsent(Paths.get(pathToJar));
    }

    public Jar getFor(String nodeId, String fileName) {
        return getFor(nodeId, Paths.get(fileName));
    }

    public Jar getFor(String nodeId, Path fileName) {
        return findAndStoreIfAbsent(nodeIdFactory.getCurrentNodeId().equals(nodeId) ? fileName : Paths.get(nodeId).resolve(fileName));
    }
}
