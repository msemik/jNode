package pl.edu.uj.cluster.node;

import pl.edu.uj.cluster.message.PrimaryHeartBeat;

import static java.lang.Math.max;

/**
 * Created by alanhawrot on 02.03.2016.
 */
public class Node implements Comparable<Node> {
    private static Distance distance;
    private String nodeId;
    private int poolSize;
    private int availableThreads;
    private double priority;
    private boolean isCurrent;
    /**
     * Order in which node entered cluster.
     */
    private int arrivalOrder;

    public static void setDistance(Distance dst) {
        distance = dst;
    }

    protected Node(String nodeId) {
        this(nodeId, 0, 0);
    }

    public Node(String nodeId, boolean isCurrent) {
        this.nodeId = nodeId;
        this.isCurrent = isCurrent;
    }

    protected Node(String nodeId, int availableThreads) {
        this(nodeId, availableThreads, 0);
    }

    protected Node(String nodeId, int availableWorkers, double priority) {
        this.nodeId = nodeId;
        this.availableThreads = availableWorkers;
        this.priority = priority;
    }

    protected Node(String nodeId, PrimaryHeartBeat primaryHeartBeat) {
        this.nodeId = nodeId;
        poolSize = (int) primaryHeartBeat.getPoolSize();
        availableThreads = (int) max(primaryHeartBeat.getPoolSize() - primaryHeartBeat.getJobsInPool(), 0);
    }

    @Override
    public int compareTo(Node o) {
        return priority < o.getPriority() ? -1 : priority > o.getPriority() ? 1 : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return nodeId.equals(node.nodeId);
    }

    @Override
    public String toString() {
        return "{" +
                "nodeId=" + nodeId +
                ", availableThreads=" + availableThreads +
                ", poolSize=" + poolSize +
                ", priority=" + priority +
                '}';
    }

    public String getNodeId() {
        return nodeId;
    }

    public int getAvailableThreads() {
        return availableThreads;
    }

    public double getPriority() {
        return priority;
    }

    public int getArrivalOrder() {
        return arrivalOrder;
    }

    /**
     * This method is based on assumption that if priority of node is 0 then available threads is 0 either.
     */
    public boolean canTakeTasks() {
        return getPriority() > 0;
    }

    public void drainThread() {
        --availableThreads;
    }

    public void returnThread() {
        ++availableThreads;
    }

    public void setArrivalOrderAs(Node oldVersion) {
        this.arrivalOrder = oldVersion.arrivalOrder;
    }

    public int distanceBetween(Node other) {
        return distance.between(this, other);
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setArrivalOrder(int arrivalOrder) {
        this.arrivalOrder = arrivalOrder;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
