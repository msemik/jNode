package pl.edu.uj.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.edu.uj.JNodeApplication;
import pl.edu.uj.cluster.message.PrimaryHeartBeat;
import pl.edu.uj.cluster.node.Node;
import pl.edu.uj.cluster.node.NodeFactory;
import pl.edu.uj.cluster.node.Nodes;
import pl.edu.uj.engine.workerpool.WorkerPool;

import static java.lang.Math.max;

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

    @Scheduled(fixedDelay = 50, initialDelay = 200)
    public void handleOutgoing() {
        if (!application.isInitialized())
            return;
        PrimaryHeartBeat heartBeat = PrimaryHeartBeat.create(workerPool.poolSize(), workerPool.jobsInPool());
        if (last.equals(heartBeat))
            return;

        messageGateway.send(heartBeat);
        last = heartBeat;
    }

    public void handleIncoming(String sourceNodeId, PrimaryHeartBeat primaryHeartBeat) {
        Node currentNode = nodeFactory.createNode(sourceNodeId, primaryHeartBeat);
        nodes.updateAfterHeartBeat(currentNode);
    }
}
