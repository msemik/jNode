package pl.edu.uj.cluster.messages;

import java.io.Serializable;

public class CancelJarJobs implements Serializable {
    private String jarPath;

    public CancelJarJobs(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getJarPath() {
        return jarPath;
    }
}
