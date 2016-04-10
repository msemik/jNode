package pl.edu.uj.jnode.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.cluster.message.PrimaryHeartBeat;
import pl.edu.uj.jnode.cluster.node.Node;
import pl.edu.uj.jnode.cluster.node.NodeFactory;
import pl.edu.uj.jnode.cluster.node.Nodes;
import pl.edu.uj.jnode.engine.workerpool.WorkerPool;
import pl.edu.uj.jnode.main.JNodeApplication;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class HeartBeatHandler {
    static Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);
    @Autowired
    private WorkerPool workerPool;
    @Autowired
    private MessageGateway messageGateway;
    @Autowired
    private JNodeApplication application;
    @Autowired
    private Nodes nodes;
    @Autowired
    private NodeFactory nodeFactory;
    private PrimaryHeartBeat last = PrimaryHeartBeat.empty();
    private AtomicBoolean forceHeartBeat = new AtomicBoolean();

    @Scheduled(fixedDelay = 50, initialDelay = 200)
    public void handleOutgoing() {
        if (!application.isInitialized()) {
            return;
        }
        PrimaryHeartBeat heartBeat = PrimaryHeartBeat.create(workerPool.poolSize(), workerPool.jobsInPool());
        if (!forceHeartBeat.getAndSet(false) && last.equals(heartBeat)) {
            return;
        }

        messageGateway.send(heartBeat);
        last = heartBeat;
    }

    public void handleIncoming(String sourceNodeId, PrimaryHeartBeat primaryHeartBeat) {
        Node currentNode = nodeFactory.createNode(sourceNodeId, primaryHeartBeat);
        nodes.updateAfterHeartBeat(currentNode);
    }

    /**
     * Used to force heart beat even if it didn't change since last HeartBeat
     */
    public void forceOutgoing() {
        forceHeartBeat.set(true);
    }
}
