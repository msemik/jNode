package pl.edu.uj.cluster;

/**
 * Created by alanhawrot on 02.03.2016.
 */
public class NodeInfo implements Comparable<NodeInfo> {
    private String nodeId;
    private int availableThreads;
    private double priority;

    public NodeInfo(String nodeId, int availableWorkers, double priority) {
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

    public void setAvailableThreads(int availableThreads) {
        this.availableThreads = availableThreads;
    }

    @Override
    public int compareTo(NodeInfo o) {
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

        NodeInfo nodeInfo = (NodeInfo) o;

        return nodeId.equals(nodeInfo.nodeId);
    }
}