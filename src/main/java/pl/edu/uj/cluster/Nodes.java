package pl.edu.uj.cluster;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Created by alanhawrot on 02.03.2016.
 */
@Component
public class Nodes {
    /**
     * List of nodes (without current node), sorted descending by its priority.
     */
    private List<Node> nodes = new ArrayList<>();

    public synchronized boolean add(Node node) {
        boolean hasChanged = nodes.add(node);
        Collections.sort(nodes, Collections.reverseOrder());
        return hasChanged;
    }

    public synchronized boolean contains(Node node) {
        return nodes.contains(node);
    }

    public synchronized boolean remove(Node node) {
        return nodes.remove(node);
    }

    public synchronized void updateAfterHeartBeat(Node node) {
        int index = nodes.indexOf(node);
        if (index != -1) {
            nodes.set(index, node);
            Collections.sort(nodes, Collections.reverseOrder());
        } else { //For safety.
            add(node);
        }
    }

    public synchronized Optional<Node> drainThreadFromNodeHavingHighestPriority() {
        if (nodes.isEmpty()) {
            return empty();
        }
        Node firstNode = nodes.get(0);
        if (!firstNode.canTakeTasks()) {
            return empty();
        }
        firstNode.drainThread();
        if (nodes.size() > 1) {
            Node secondNode = nodes.get(1);
            if (secondNode.hasHigherPriorityThan(firstNode)) {
                Collections.swap(nodes, 0, 1);
            }
        }
        return of(firstNode);
    }

    /**
     * Proper return of a free thread happens when we haven't received any update from HeartBeat after draining a Thread.
     * Luckily we update node by migrating object, so we won't change currently valid node with valid number of threads.
     * If this semantics change in updateAfterHeartBeat, changes should be introduced in this method either.
     */
    public synchronized void returnThread(Node node) {
        node.returnThread();
    }

    @Override
    public String toString() {
        return nodes.stream().map(Node::toString).collect(Collectors.joining("\n"));
    }
}
