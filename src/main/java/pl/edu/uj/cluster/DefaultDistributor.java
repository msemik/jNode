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

import java.util.List;
import java.util.concurrent.BlockingQueue;

import static pl.edu.uj.cluster.DefaultDistributor.TaskDelegationState.*;

/**
 * Created by alanhawrot on 01.03.2016.
 */
@Component
public class DefaultDistributor implements Distributor {
    private final Object taskDelegationLock = new Object();
    private TaskDelegationState delegationState = NO_DELEGATION;
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
    private Nodes nodes;
    @Autowired
    private MessageGateway messageGateway;

    @Override
    public void onWorkerPoolOverflow(WorkerPoolOverflowEvent event) {
        synchronized (taskDelegationLock) {
            if (delegationState == NO_DELEGATION)
                delegationState = DURING_DELEGATION;
            else {
                if (delegationState == DURING_DELEGATION)
                    delegationState = DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION;
                return;
            }
        }

        delegateTasks();
    }

    private void delegateTasks() {
        while (true) {
            BlockingQueue<Runnable> awaitingTasks = workerPool.getAwaitingTasks();
            int expectedFreeThreadsNumber = awaitingTasks.size(); //There is no reason to keep this synchronized
            if (expectedFreeThreadsNumber == 0) {                 //as pool may drain tasks
                synchronized (taskDelegationLock) {
                    delegationState = NO_DELEGATION;
                }
                return;
            }

            List<Node> selectedNodes = nodes.getMinHaving(expectedFreeThreadsNumber);
            if (selectedNodes.isEmpty()) {                        //No need to keep it synchronized, as it doesn't change
                synchronized (taskDelegationLock) {
                    delegationState = AWAITING_FREE_THREADS;
                }
                return;
            }

            for (Node selectedNode : selectedNodes) {
                for (int i = 0; i < selectedNode.getAvailableThreads(); i++) {
                    WorkerPoolTask task = (WorkerPoolTask) awaitingTasks.poll();
                    if (task == null) {                           //Synchronizing it doesn't change anything.
                        synchronized (taskDelegationLock) {
                            delegationState = NO_DELEGATION;
                        }
                        return;
                    }
                    if (!delegateTask(selectedNode, task))
                        --i;
                }
            }

            synchronized (taskDelegationLock) {
                if (delegationState == DURING_DELEGATION) { // No more tasks and free threads appeared
                    delegationState = NO_DELEGATION;
                    return;
                }
                //current state is DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION
                delegationState = DURING_DELEGATION;
            }
        }
    }

    private boolean delegateTask(Node destinationNode, WorkerPoolTask task) {
        String destinationNodeId = destinationNode.getNodeId();
        int taskId = task.getTaskId();
        if (task.isExternal()) {
            ExternalTask externalTask = (ExternalTask) task;
            if (!externalTaskRegistry.remove(externalTask)) {
                logger.info("There is no entry for given " + externalTask);
                logger.info("Not sending any Sry or Redirect message for " + externalTask);
                return false;
            }

            if (externalTask.isOriginatedAt(destinationNode))
                messageGateway.send(new Sry(taskId), destinationNodeId);
            else
                messageGateway.send(new Redirect(destinationNodeId, taskId), externalTask.getSourceNodeId());
        } else {
            DelegatedTask delegatedTask = new DelegatedTask(task, destinationNodeId);
            delegatedTaskRegistry.add(delegatedTask);

            ExternalTask externalTask = new ExternalTask(task, messageGateway.getCurrentNodeId());
            messageGateway.send(new TaskDelegation(externalTask), destinationNodeId);
        }
        return true;
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
        // TODO update Nodes

        boolean shallDelegateTasks = false;
        synchronized (taskDelegationLock) {
            if (delegationState == DURING_DELEGATION)                             //New threads may produce more delegations
                delegationState = DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION;
            else if (delegationState == AWAITING_FREE_THREADS) {                 //Delegations are waiting but there is no free threads
                delegationState = DURING_DELEGATION;
                shallDelegateTasks = true;
            }
        }
        if (shallDelegateTasks)
            delegateTasks();
    }

    @Override
    public void onJarRequest(String nodeId, String jarFileName) {

    }

    @Override
    public void onJarDelivery(String nodeId, String jarFileName, byte[] jar) {

    }

    enum TaskDelegationState {
        NO_DELEGATION,
        DURING_DELEGATION,
        DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION,
        AWAITING_FREE_THREADS;
    }
}
