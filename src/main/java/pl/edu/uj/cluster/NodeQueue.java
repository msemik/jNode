package pl.edu.uj.cluster;

import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by alanhawrot on 02.03.2016.
 */
@Component
public class NodeQueue {
    private Queue<NodeInfo> priorityQueue = new PriorityBlockingQueue<>();

    // TODO: add necessary methods
}
