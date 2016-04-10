package pl.edu.uj.jnode.cluster.message;

import pl.edu.uj.jnode.cluster.Distributable;
import pl.edu.uj.jnode.cluster.Distributor;

import java.io.Serializable;
import java.util.Optional;

public class JarDelivery implements Serializable, Distributable {
    private byte[] jar;
    private String fileName;

    public JarDelivery(byte[] jar, String fileName) {
        this.jar = jar;
        this.fileName = fileName;
    }

    public byte[] getJar() {
        return jar;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onJarDelivery(sourceNodeId, fileName, jar);
    }

    @Override
    public String toString() {
        return "JarDelivery{" + fileName + '}';
    }
}
