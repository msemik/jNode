package pl.edu.uj.cluster.messages;

import pl.edu.uj.cluster.Distributor;

import java.util.Optional;

public interface Distributable {

    void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId);
}
