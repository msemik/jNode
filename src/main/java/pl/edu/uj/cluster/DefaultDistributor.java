package pl.edu.uj.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pl.edu.uj.cluster.messages.PrimaryHeartBeat;
import pl.edu.uj.cluster.messages.Redirect;
import pl.edu.uj.cluster.messages.Sry;
import pl.edu.uj.cluster.messages.TaskDelegation;
import pl.edu.uj.engine.CancelJarJobsEvent;
import pl.edu.uj.engine.TaskCancelledEvent;
import pl.edu.uj.engine.TaskFinishedEvent;
import pl.edu.uj.engine.workerpool.WorkerPool;
import pl.edu.uj.engine.workerpool.WorkerPoolOverflowEvent;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
    private NodeList nodeList;
    @Autowired
    private MessageGateway messageGateway;

    @Override
    public synchronized void onWorkerPoolOverflow(WorkerPoolOverflowEvent event) {
        Queue<Runnable> awaitingTasks = workerPool.getAwaitingTasks();

        List<Node> selectedNodes = nodeList.getMinNodeList(awaitingTasks);
        for (Node selectedNode : selectedNodes) {
            for (int i = 0; i < selectedNode.getAvailableThreads(); i++) {
                WorkerPoolTask task = (WorkerPoolTask) awaitingTasks.poll();
                if (task == null) {
                    return;
                }
                if (task.isExternal()) {
                    ExternalTask externalTask = (ExternalTask) task;
                    if (!externalTaskRegistry.remove(externalTask)) {
                        logger.info("There is no entry for given " + externalTask);
                        logger.info("Not sending any Sry or Redirect message for " + externalTask);
                        continue;
                    }

                    if (externalTask.getSourceNodeId().compareTo(selectedNode.getNodeId()) == 0) {
                        messageGateway.send(new Sry(externalTask.getTaskId()), selectedNode.getNodeId());
                    } else {
                        messageGateway.send(new Redirect(selectedNode.getNodeId(), externalTask.getTaskId()), externalTask.getSourceNodeId());
                    }
                } else {
                    DelegatedTask delegatedTask = new DelegatedTask(task, selectedNode.getNodeId());
                    delegatedTaskRegistry.add(delegatedTask);

                    ExternalTask externalTask = new ExternalTask(task, "MyNodeId"); // TODO: getSourceNodeId (local). Distributor has to know local node id.
                    messageGateway.send(new TaskDelegation(externalTask), selectedNode.getNodeId());
                }
            }
        }
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

    @Override
    public void onCancelJarJobs(CancelJarJobsEvent event) {

    }

    @Override
    public void onTaskCancelled(TaskCancelledEvent event) {

    }

    @Override
    public void onNodeGone(String nodeId) {

    }

    @Override
    public void onCancelJarJobs(String sourceNodeId, String jarPath) {

    }

    @Override
    public void onPrimaryHeartBeat(String sourceNodeId, PrimaryHeartBeat primaryHeartBeat) {

    }

    @Override
    public void onJarRequest(String nodeId, String jarFileName) {

    }

    @Override
    public void onJarDelivery(String nodeId, String jarFileName, byte[] jar) {

    }
}
