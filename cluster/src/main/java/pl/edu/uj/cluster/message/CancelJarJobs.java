package pl.edu.uj.cluster.message;

import pl.edu.uj.cluster.Distributable;
import pl.edu.uj.cluster.Distributor;

import java.io.Serializable;
import java.util.Optional;

public class CancelJarJobs implements Serializable, Distributable {
    private String jarName;

    public CancelJarJobs(String jarName) {
        this.jarName = jarName;
    }

    public String getJarName() {
        return jarName;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onCancelJarJobs(sourceNodeId, jarName);
    }

    @Override
    public String toString() {
        return "CancelJarJobs{" + jarName + '}';
    }
}
