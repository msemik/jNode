package pl.edu.uj.jnode.cluster.message;

import pl.edu.uj.jnode.cluster.Distributable;
import pl.edu.uj.jnode.cluster.Distributor;

import java.io.Serializable;
import java.util.Optional;

public class JarRequest implements Serializable, Distributable {
    private String jarName;

    public JarRequest(String jarName) {
        this.jarName = jarName;
    }

    public String getJarName() {
        return jarName;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onJarRequest(sourceNodeId, jarName);
    }

    @Override
    public String toString() {
        return "JarRequest{" + jarName + '}';
    }
}
