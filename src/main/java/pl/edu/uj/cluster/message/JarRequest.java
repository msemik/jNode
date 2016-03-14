package pl.edu.uj.cluster.message;

import pl.edu.uj.cluster.Distributable;
import pl.edu.uj.cluster.Distributor;

import java.io.Serializable;
import java.util.Optional;

public class JarRequest implements Serializable, Distributable {
    private String jar;

    public JarRequest(String jar) {
        this.jar = jar;
    }

    public String getJar() {
        return jar;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onJarRequest(sourceNodeId, jar);
    }

    @Override
    public String toString() {
        return "JarRequest{" + jar + '}';
    }
}
