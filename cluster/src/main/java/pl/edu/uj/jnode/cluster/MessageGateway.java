package pl.edu.uj.jnode.cluster;

import java.io.Serializable;

public interface MessageGateway {
    void send(Serializable obj, String destinationNodeId);

    void send(Serializable obj);

    String getCurrentNodeId();
}
