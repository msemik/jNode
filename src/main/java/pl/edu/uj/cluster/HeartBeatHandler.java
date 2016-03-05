package pl.edu.uj.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.edu.uj.ApplicationInitializedEvent;
import pl.edu.uj.JNodeApplication;
import pl.edu.uj.cluster.messages.PrimaryHeartBeat;
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
    private PrimaryHeartBeat last = PrimaryHeartBeat.empty();

    @Scheduled(fixedDelay = 50, initialDelay = 200)
    public void handleOutgoing() {
        if (!application.isInitialized())
            return;
        PrimaryHeartBeat heartBeat = PrimaryHeartBeat.create(workerPool.jobsInPool(), workerPool.poolSize());
        if (last.equals(heartBeat))
            return;

        messageGateway.send(heartBeat);
        last = heartBeat;
    }

    public void handleIncoming(String sourceNodeId, PrimaryHeartBeat primaryHeartBeat) {
        long availableThreads = max(primaryHeartBeat.getThreadsInUse() - primaryHeartBeat.getJobsInPool(), 0);
        nodes.updateAfterHeartBeat(new Node(sourceNodeId, (int) availableThreads));
    }
}
