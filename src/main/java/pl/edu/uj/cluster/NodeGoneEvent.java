package pl.edu.uj.cluster;

import org.springframework.context.ApplicationEvent;

/**
 * Created by alanhawrot on 01.03.2016.
 */
public class NodeGoneEvent extends ApplicationEvent {
    private String nodeId;

    public NodeGoneEvent(Object source, String nodeId) {
        super(source);
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }
}
