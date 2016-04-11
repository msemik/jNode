package pl.edu.uj.crosscuting;

import java.net.URL;
import java.nio.file.*;

public class Resources
{
    private Resources()
    {

    }

    public static Resources getInstance()
    {
        return new Resources();
    }

    public String getPathAsString(String path)
    {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        String file = url.getFile();
        return file;
    }

    public Path getPath(String path)
    {
        String pathAsString = getPathAsString(path);
        return Paths.get(pathAsString);
    }
}
