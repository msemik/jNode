package pl.edu.uj.cluster.messages;

import pl.edu.uj.cluster.Distributor;

import java.io.Serializable;
import java.util.Optional;

public class JarRequest implements Serializable, Distributable {
    private String jarFileName;

    public JarRequest(String jarFileName) {
        this.jarFileName = jarFileName;
    }

    public String getJarFileName() {
        return jarFileName;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onJarRequest(sourceNodeId, jarFileName);
    }
}