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
    private Queue<NodeInfo> priorityQueue = new PriorityBlockingQueue<>();

    public boolean add(NodeInfo nodeInfo) {
        return priorityQueue.add(nodeInfo);
    }

    public boolean remove(NodeInfo nodeInfo) {
        return priorityQueue.remove(nodeInfo);
    }

    public NodeInfo[] toArray() {
        NodeInfo[] nodes = (NodeInfo[]) priorityQueue.toArray();
        Arrays.sort(nodes);
        return nodes;
    }
}