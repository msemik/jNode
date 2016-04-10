package pl.edu.uj.crosscutting;

import java.net.URL;

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
}
