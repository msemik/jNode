package pl.edu.uj.cluster;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by alanhawrot on 02.03.2016.
 */
@Component
public class NodeList {
    private List<Node> nodeList = new ArrayList<>();

    public synchronized boolean add(Node node) {
        boolean hasChanged = nodeList.add(node);
        Collections.sort(nodeList, Collections.reverseOrder());
        return hasChanged;
    }

    public synchronized boolean contains(Node node) {
        return nodeList.contains(node);
    }

    public synchronized boolean remove(Node node) {
        return nodeList.remove(node);
    }

    public synchronized void update(Node node) {
        int index = nodeList.indexOf(node);
        if (index != -1) {
            nodeList.set(index, node);
            Collections.sort(nodeList, Collections.reverseOrder());
        }
    }

    public synchronized List<Node> getMinNodeList(long awaitingTasks) {
        List<Node> selectedNodes = new LinkedList<>();
        long availableThreadsSum = 0;
        for (int i = 0; i < nodeList.size() && availableThreadsSum < awaitingTasks; i++) {
            if (nodeList.get(i).getPriority() > 0) {
                selectedNodes.add(nodeList.get(i));
                availableThreadsSum += nodeList.get(i).getAvailableThreads();
            }
        }
        return selectedNodes;
    }
}
