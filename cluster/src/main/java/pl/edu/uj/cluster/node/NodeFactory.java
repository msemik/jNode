package pl.edu.uj.cluster.node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.uj.cluster.MessageGateway;
import pl.edu.uj.cluster.message.PrimaryHeartBeat;

import java.util.List;

@Component
public class NodeFactory {
    @Autowired
    private MessageGateway messageGateway;

    public Priority createPriority(List<Node> nodes) {
        return new NearestDistancePriority();
    }

    public void initializeDistance(List<Node> nodes) {
        Node.setDistance(new ArrivalDistance(nodes));
    }

    public Node createCurrentNode() {
        return new Node(messageGateway.getCurrentNodeId(), true);
    }

    public Node createNode(String newNodeId) {
        return new Node(newNodeId);
    }

    public Node createNode(String sourceNodeId, PrimaryHeartBeat primaryHeartBeat) {
        return new Node(sourceNodeId, primaryHeartBeat);
    }
}
