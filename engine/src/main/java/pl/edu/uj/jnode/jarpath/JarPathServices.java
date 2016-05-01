package pl.edu.uj.jnode.jarpath;

import org.apache.commons.lang3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pl.edu.uj.jnode.main.JNodeApplication;
import pl.edu.uj.jnode.main.OptionsDispatchedEvent;
import pl.edu.uj.jnode.main.options.CustomJarPathEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Created by michal on 22.10.15.
 */
@Service
public class JarPathServices {
    @Autowired
    private JarFactory jarFactory;
    private Path pathToJarPath;

    @EventListener
    public void on(CustomJarPathEvent event) {
        init(event.getCustomJarPath());
    }

    private void init(Path path) {
        if (this.pathToJarPath != null) {
            return;
        }

        if (path.isAbsolute()) {
            pathToJarPath = path;
        } else {
            String applicationDir = JNodeApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            if(SystemUtils.IS_OS_WINDOWS && applicationDir.startsWith("/"))
                applicationDir = applicationDir.substring(1);
            pathToJarPath = Paths.get(applicationDir);
            if (StringUtils.endsWithAny(pathToJarPath.toString(), ".jar", "classes")) {
                pathToJarPath = pathToJarPath.getParent().resolve(path);
            }
        }

        if (Files.notExists(pathToJarPath)) {
            try {
                Files.createDirectory(pathToJarPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Files.isDirectory(pathToJarPath)) {
            throw new IllegalArgumentException("Path: '" + pathToJarPath + "' is not a directory");
        }
        validateReadWriteAccess(pathToJarPath);
    }

    public void validateReadWriteAccess(Path path) {
        if (!Files.isReadable(path)) {
            throw new IllegalStateException("Read access denied for path '" + path + "'");
        }

        if (!Files.isWritable(path)) {
            throw new IllegalStateException("Write access denied for path '" + path + "'");
        }
    }

    @EventListener
    public void on(OptionsDispatchedEvent event) {
        init(Paths.get("jarpath"));
    }

    public Optional<Jar> getJarForProperty(Path path) {
        String strPath = path.toString();
        if (!strPath.endsWith(".properties")) {
            throw new IllegalArgumentException("not a property file '" + path + "'");
        }
        String pathToJar = strPath.substring(0, strPath.length() - ".properties".length()) + ".pathToJar";
        try {
            return of(jarFactory.getFor(pathToJar));
        } catch (IllegalArgumentException e) {
            return empty();
        }
    }

    public boolean isJarOrProperty(Path path) {
        return isValidExistingJar(path) || isProperties(path);
    }

    public boolean isValidExistingJar(Path path) {
        try {
            return jarFactory.getFor(path).isValidExistingJar();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isProperties(Path path) {
        return Files.isRegularFile(path) && path.toString().endsWith(".properties");
    }

    public Path getPathSinceJarPath(Path pathToJarInJarPath) {
        Path relativized = getJarPath().relativize(pathToJarInJarPath);
        System.out.println(pathToJarInJarPath + " since " + getJarPath() + " is " + relativized);
        return relativized;
    }

    public Path getJarPath() {
        return pathToJarPath;
    }
}
