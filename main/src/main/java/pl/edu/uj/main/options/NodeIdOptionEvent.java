package pl.edu.uj.main.options;

import org.springframework.context.ApplicationEvent;

/**
 * Created by michal on 21.10.15.
 */
public class NodeIdOptionEvent extends ApplicationEvent {
    private final String nodeId;

    public NodeIdOptionEvent(Object source, String nodeId) {
        super(source);
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }
}
