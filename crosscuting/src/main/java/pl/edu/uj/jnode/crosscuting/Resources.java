package pl.edu.uj.jnode.crosscuting;

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
        return file;
    }

    public Path getPath(String path) {
        String pathAsString = getPathAsString(path);
        return Paths.get(pathAsString);
    }
}
