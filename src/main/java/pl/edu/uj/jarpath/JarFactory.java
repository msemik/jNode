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

    public Jar getFor(String pathToJar) {
        return findAndStoreIfAbsent(Paths.get(pathToJar));
    }

    public Jar getFor(String nodeId, Path fileName) {
        return findAndStoreIfAbsent(nodeIdFactory.getCurrentNodeId().equals(nodeId) ? fileName : Paths.get(nodeId).resolve(fileName));
    }

    public Jar getFor(String nodeId, String fileName) {
        return getFor(nodeId, Paths.get(fileName));
    }

    private Jar findAndStoreIfAbsent(Path pathToJar) {
        pathToJar = pathToJar.normalize();
        Jar jar = find(pathToJar);
        if (jar == null) {
            if (pathToJar.isAbsolute()) {
                pathToJar = jarPathServices.getPathSinceJarPath(pathToJar).normalize();
            }

            String nodeId;
            if (pathToJar.getNameCount() == 1) { //its local fileName, its in root directory of jarpath
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
        return jars.stream()
                .filter(jar -> jar.hasRelativePath(pathToJar))
                .findAny()
                .orElse(null);
    }

}
