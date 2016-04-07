package pl.edu.uj.context;

import pl.edu.uj.jarpath.Jar;

import java.nio.file.Path;

public class MyJar extends Jar
{

    public MyJar(String nodeId, Path pathRelativeToJarPath)
    {
        super(nodeId, pathRelativeToJarPath);
    }
}
