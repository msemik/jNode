package pl.edu.uj.cluster.messages;

import pl.edu.uj.cluster.Distributor;

import java.io.Serializable;
import java.util.Optional;

public class CancelJarJobs implements Serializable, Distributable {
    private String jarPath;

    public CancelJarJobs(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getJarPath() {
        return jarPath;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onCancelJarJobs(sourceNodeId, jarPath);
    }

    @Override
    public String toString() {
        return "CancelJarJobs{" + jarPath + '}';
    }
}
