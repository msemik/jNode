package pl.edu.uj.cluster.node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.ApplicationInitializedEvent;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;
import static java.util.Collections.sort;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Created by alanhawrot on 02.03.2016.
 */
@Component
public class Nodes {

    @Autowired
    private NodeFactory nodeFactory;

    /**
     * List of node (without current node), may be in no particular order (its ordered on demand)
     */
    private List<Node> nodes = new ArrayList<>();


    @EventListener
    public void on(ApplicationInitializedEvent event) {
        nodeFactory.initializeDistance(nodes);
        Node currentNode = nodeFactory.createCurrentNode();
        add(currentNode);
    }

    public synchronized void add(Node node) {
        node.setArrivalOrder(nodes.size());
        nodes.add(node);
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
            Node oldVersion = nodes.set(index, node);
            node.setArrivalOrderAs(oldVersion);
        } else { //For safety.
            add(node);
        }
    }

    public synchronized Optional<Node> drainThreadFromNodeHavingHighestPriority() {
        if (nodes.size() <= 1) {
            return empty();
        }
        computePriorities();
        Node firstNode = nodes.get(0);
        if (!firstNode.canTakeTasks()) {
            return empty();
        }
        firstNode.drainThread();
        return of(firstNode);
    }

    private void computePriorities() {
        Priority priority = nodeFactory.createPriority(nodes);
        for (Node node : nodes) {
            node.computePriority(priority);
        }
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
