package pl.edu.uj.cluster.node;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Integer.signum;

/**
 * Prioritize distance. if two node has same distance, higher priority gets node with more availaible threads.
 */
public class NearestDistancePriority implements Priority {

    public NearestDistancePriority() {
    }

    @Override
    public void calculate(List<Node> nodes) {
        Node currentNode = getCurrentNode(nodes);
        int maxPoolSize = computeMaxPoolSize(nodes);
        List<Integer> distances = computeDistancesBetween(nodes, currentNode);
        int maxDistance = maxDistance(distances);

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node.isCurrent())
                continue;
            int dst = distances.get(i);
            int availableThreads = node.getAvailableThreads();
            node.setPriority(signum(availableThreads) * (maxDistance - dst + 1) + availableThreads / (maxPoolSize + 1));
        }
    }

    private Node getCurrentNode(List<Node> nodes) {
        return nodes.stream()
                .filter(Node::isCurrent)
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Expected current node in the list"));
    }

    private int computeMaxPoolSize(List<Node> nodes) {
        return nodes.stream().mapToInt(Node::getPoolSize).max().orElse(0);
    }

    private List<Integer> computeDistancesBetween(List<Node> nodes, Node node) {
        return nodes.stream().map(node::distanceBetween).collect(Collectors.toList());
    }

    private int maxDistance(List<Integer> distances) {
        return distances.stream().mapToInt(Integer::intValue).max().orElse(0);
    }
}
