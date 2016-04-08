package pl.edu.uj.cluster;

import java.io.Serializable;

public interface MessageGateway {
    void send(Serializable obj, String destinationNodeId);

    void send(Serializable obj);

    String getCurrentNodeId();
}
