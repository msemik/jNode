package pl.edu.uj.cluster;

import java.util.Optional;

public interface Distributable {
    void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId);
}
