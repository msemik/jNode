package pl.edu.uj.cluster;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by alanhawrot on 02.03.2016.
 */
@Component
public class Nodes {
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

    public synchronized void update(Node node) { //TODO: wtf?
        int index = nodes.indexOf(node);
        if (index != -1) {
            nodes.set(index, node);
            Collections.sort(nodes, Collections.reverseOrder());
        }
    }

    public synchronized List<Node> getMinHaving(long expectedFreeThreadsNumber) {
        List<Node> selectedNodes = new LinkedList<>();
        long availableThreadsSum = 0;
        for (int i = 0; i < nodes.size() && availableThreadsSum < expectedFreeThreadsNumber; i++) {
            Node node = nodes.get(i);
            if (!node.canTakeTasks())
                return selectedNodes;

            selectedNodes.add(node);
            availableThreadsSum += node.getAvailableThreads();

        }
        return selectedNodes;
    }
}
