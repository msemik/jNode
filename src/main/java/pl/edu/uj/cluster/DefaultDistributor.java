package pl.edu.uj.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.cluster.delegation.DelegationHandler;
import pl.edu.uj.cluster.message.PrimaryHeartBeat;
import pl.edu.uj.cluster.node.Node;
import pl.edu.uj.cluster.node.NodeFactory;
import pl.edu.uj.cluster.node.Nodes;
import pl.edu.uj.cluster.task.DelegatedTaskRegistry;
import pl.edu.uj.cluster.task.ExternalTask;
import pl.edu.uj.cluster.task.ExternalTaskRegistry;
import pl.edu.uj.engine.event.CancelJarJobsEvent;
import pl.edu.uj.engine.event.TaskCancelledEvent;
import pl.edu.uj.engine.event.TaskFinishedEvent;
import pl.edu.uj.engine.workerpool.WorkerPool;
import pl.edu.uj.engine.workerpool.WorkerPoolOverflowEvent;

/**
 * Created by alanhawrot on 01.03.2016.
 */
@Component
public class DefaultDistributor implements Distributor {
    private Logger logger = LoggerFactory.getLogger(DefaultDistributor.class);
    @Autowired
    private DelegatedTaskRegistry delegatedTaskRegistry;
    @Autowired
    private ExternalTaskRegistry externalTaskRegistry;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private WorkerPool workerPool;
    @Autowired
    private MessageGateway messageGateway;
    @Autowired
    private DelegationHandler delegationHandler;
    @Autowired
    private HeartBeatHandler heartBeatHandler;
    @Autowired
    private Nodes nodes;
    @Autowired
    private NodeFactory nodeFactory;

    @Override
    @EventListener
    public void on(WorkerPoolOverflowEvent event) {
        delegationHandler.handleDuringOnWorkerPoolEvent();
    }


    @Override
    public void onTaskDelegation(ExternalTask externalTask) {

    }

    @Override
    public void onRedirect(String currentNodeId, String destinationNodeId, long taskId) {

    }

    @Override
    public void onSry(String nodeId, long taskId) {

    }

    @Override
    public void onTaskFinished(TaskFinishedEvent event) {

    }

    @Override
    public void onTaskExecutionCompleted(long taskId, Object taskResult) {

    }

    @Override
    public void onTaskExecutionCompleted(long taskId, Throwable exception) {

    }

    @EventListener
    @Override
    public void on(CancelJarJobsEvent event) {

    }

    @Override
    @EventListener
    public void on(TaskCancelledEvent event) {

    }

    @Override
    public void onNodeGone(String nodeId) {

    }

    @Override
    public void onNewNode(String newNodeId) {
        Node newNode = nodeFactory.createNode(newNodeId);
        nodes.add(newNode);
    }

    @Override
    public void onCancelJarJobs(String sourceNodeId, String jarPath) {

    }

    @Override
    public void onPrimaryHeartBeat(String sourceNodeId, PrimaryHeartBeat primaryHeartBeat) {
        heartBeatHandler.handleIncoming(sourceNodeId, primaryHeartBeat);
        delegationHandler.handleDuringOnHeartBeat();
    }

    @Override
    public void onJarRequest(String nodeId, String jarFileName) {

    }

    @Override
    public void onJarDelivery(String nodeId, String jarFileName, byte[] jar) {

    }
}
