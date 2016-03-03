package pl.edu.uj.cluster;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by alanhawrot on 02.03.2016.
 */
@Component
public class NodePriorityQueue {
    private Queue<Node> priorityQueue = new PriorityBlockingQueue<>();

    public boolean add(Node node) {
        return priorityQueue.add(node);
    }

    public boolean remove(Node node) {
        return priorityQueue.remove(node);
    }

    public Node[] toArray() {
        Node[] nodes = (Node[]) priorityQueue.toArray();
        Arrays.sort(nodes);
        return nodes;
    }
}
