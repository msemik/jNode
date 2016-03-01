package pl.edu.uj.cluster.messages;

import java.io.Serializable;

public class JarRequest implements Serializable {
    private String jarFileName;

    public JarRequest(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public String getJarFileName() {
        return jarFileName;
    }
}
