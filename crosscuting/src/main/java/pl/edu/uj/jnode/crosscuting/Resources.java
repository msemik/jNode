package pl.edu.uj.jnode.crosscuting;

import org.apache.commons.lang3.SystemUtils;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Resources {
    private Resources() {

    }

    public static Resources getInstance() {
        return new Resources();
    }

    public String getPathAsString(String path) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        String file = url.getFile();
        if (SystemUtils.IS_OS_WINDOWS) {
            if (file.startsWith("/")) {
                return file.substring(1);
            }
        }
        return file;
    }

    public Path getPath(String path) {
        String pathAsString = getPathAsString(path);
        return Paths.get(pathAsString);
    }
}
