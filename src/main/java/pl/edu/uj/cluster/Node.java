package pl.edu.uj.cluster;

/**
 * Created by alanhawrot on 02.03.2016.
 */
public class Node implements Comparable<Node> {
    private String nodeId;
    private int availableThreads;
    private double priority;

    public Node(String nodeId, int availableThreads) {
        this.nodeId = nodeId;
        this.availableThreads = availableThreads;
    }

    public Node(String nodeId, int availableWorkers, double priority) {
        this.nodeId = nodeId;
        this.availableThreads = availableWorkers;
        this.priority = priority;
    }

    public String getNodeId() {
        return nodeId;
    }

    public int getAvailableThreads() {
        return availableThreads;
    }

    @Override
    public int compareTo(Node o) {
        return priority < o.getPriority() ? -1 : priority > o.getPriority() ? 1 : 0;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return nodeId.equals(node.nodeId);
    }

    /**
     * This method is based on assumption that if priority of node is 0 then available threads is 0 either.
     */
    public boolean canTakeTasks() {
        return getPriority() > 0;
    }

    public boolean hasHigherPriorityThan(Node node) {
        return getPriority() > node.getPriority();
    }

    public void drainThread() {
        --availableThreads;
    }

    public void returnThread() {
        ++availableThreads;
    }

    public void setAvailableThreads(int availableThreads) {
        this.availableThreads = availableThreads;
    }
}
