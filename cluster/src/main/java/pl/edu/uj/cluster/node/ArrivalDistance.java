package pl.edu.uj.cluster.node;

import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.min;

/**
 * Computes distance between node in ring.
 * Nodes are ordered by their arrival order in cluster.
 */
public class ArrivalDistance implements Distance {
    private List<Node> nodes;

    protected ArrivalDistance(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public int between(Node n1, Node n2) {
        int absDst = abs(n1.getArrivalOrder() - n2.getArrivalOrder());
        int dstInRing = min(absDst, nodes.size() - absDst);
        return dstInRing;
    }
}
