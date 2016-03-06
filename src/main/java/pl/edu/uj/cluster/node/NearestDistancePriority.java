package pl.edu.uj.cluster.node;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Integer.signum;

/**
 * Prioritize distance. if two node has same distance, higher priority gets node with more availaible threads.
 */
public class NearestDistancePriority implements Priority {

    private final List<Node> nodes;

    public NearestDistancePriority(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public double computeFor(Node node) {
        int maxPoolSize = computeMaxPoolSize();
        double partialPriority = 0;
        List<Integer> distances = computeDistancesBetween(node);
        int maxDistance = maxDistance(distances);
        for (int i = 0; i < nodes.size(); i++) {
            Node otherNode = nodes.get(i);
            if (otherNode.equals(node))
                continue;
            int dist = distances.get(i);
            int availableThreadsInOtherNode = otherNode.getAvailableThreads();

            partialPriority += signum(availableThreadsInOtherNode) * (maxDistance - dist);
            partialPriority += availableThreadsInOtherNode / (maxPoolSize + 1);
        }
        return partialPriority;
    }

    private int computeMaxPoolSize() {
        return nodes.stream().mapToInt(Node::getPoolSize).max().orElse(0);
    }

    private int maxDistance(List<Integer> distances) {
        return distances.stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    private List<Integer> computeDistancesBetween(Node node) {
        return nodes.stream().map(node::distanceBetween).collect(Collectors.toList());
    }
}
