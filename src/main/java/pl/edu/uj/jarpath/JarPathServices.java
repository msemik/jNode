package pl.edu.uj.jarpath;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.edu.uj.JNodeApplication;
import pl.edu.uj.options.CustomJarPathEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Created by michal on 22.10.15.
 */
@Service
public class JarPathServices {
    private Path pathToJarPath = Paths.get("jarpath");

    @EventListener
    public void on(CustomJarPathEvent event) {
        pathToJarPath = Paths.get(event.getCustomJarPath());
    }


    public Optional<Path> getJarForProperty(Path path) {
        String s = path.toString();
        if (!s.endsWith(".properties"))
            throw new IllegalArgumentException("not a property file '" + path + "'");
        String jarFileName = s.substring(0, s.length() - ".properties".length()) + ".jar";
        Path jarPath = Paths.get(jarFileName);
        if (Files.exists(jarPath) && Files.isRegularFile(jarPath))
            return Optional.of(jarPath);
        return Optional.empty();
    }

    public Path getPropertyForJar(Path jarPath) {
        String s = jarPath.toString();
        if (!s.endsWith(".jar"))
            throw new IllegalArgumentException("not a jar file '" + jarPath + "'");
        String propertiesFileName = s.substring(0, s.length() - ".jar".length()) + ".properties";
        return Paths.get(propertiesFileName);
    }

    public boolean isJarOrProperty(Path path) {
        return isJar(path) || isProperties(path);
    }

    public boolean isJar(Path path) {
        return Files.isRegularFile(path) && path.toString().endsWith(".jar");
    }

    public boolean isProperties(Path path) {
        return Files.isRegularFile(path) && path.toString().endsWith(".properties");
    }

    public Path getJarPath() {
        Path path;
        if (pathToJarPath.isAbsolute()) {
            path = pathToJarPath;
        } else {
            String applicationDir = JNodeApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            path = Paths.get(applicationDir);
            if (applicationDir.endsWith(".jar") || path.endsWith("classes")) {
                path = path.getParent().resolve(pathToJarPath);
            }
        }

        if (Files.notExists(path))
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        if (!Files.isDirectory(path))
            throw new IllegalArgumentException("Path: '" + path + "' is not a directory");
        validateReadWriteAccess(path);
        return path;
    }

    public void validateReadWriteAccess(Path path) {
        if (!Files.isReadable(path))
            throw new IllegalStateException("Read access denied for path '" + path + "'");

        if (!Files.isWritable(path))
            throw new IllegalStateException("Write access denied for path '" + path + "'");
    }
}
