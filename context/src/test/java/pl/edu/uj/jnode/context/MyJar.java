package pl.edu.uj.jnode.context;

import pl.edu.uj.jnode.jarpath.Jar;

import java.nio.file.Path;

public class MyJar extends Jar {
    public MyJar(String nodeId, Path pathRelativeToJarPath) {
        super(nodeId, pathRelativeToJarPath);
    }
}
