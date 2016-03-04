package pl.edu.uj.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.uj.cluster.messages.Redirect;
import pl.edu.uj.cluster.messages.Sry;
import pl.edu.uj.cluster.messages.TaskDelegation;
import pl.edu.uj.engine.workerpool.WorkerPool;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import static pl.edu.uj.cluster.TaskDelegationHandler.TaskDelegationState.*;


@Component
public class TaskDelegationHandler {
    private Logger logger = LoggerFactory.getLogger(TaskDelegationHandler.class);
    private TaskDelegationState delegationState = NO_DELEGATION;
    @Autowired
    private Nodes nodes;
    @Autowired
    private WorkerPool workerPool;
    @Autowired
    private ExternalTaskRegistry externalTaskRegistry;
    @Autowired
    private MessageGateway messageGateway;
    @Autowired
    private DelegatedTaskRegistry delegatedTaskRegistry;

    public void handleOnWorkerPoolEvent() {
        synchronized (this) {
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

    public void handleOnHeartBeat() {

        boolean shallDelegateTasks = false;
        synchronized (this) {
            if (delegationState == DURING_DELEGATION)                             //New threads may produce more delegations
                delegationState = DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION;
            else if (delegationState == AWAITING_FREE_THREADS) {                  //Delegations are waiting but there is no free threads
                delegationState = DURING_DELEGATION;
                shallDelegateTasks = true;
            }
        }
        if (shallDelegateTasks)
            delegateTasks();
    }

    /**
     * Sends tasks until there is no tasks or threads left
     */
    private void delegateTasks() {
        while (true) {
            BlockingQueue<Runnable> awaitingTasks = workerPool.getAwaitingTasks();
            int expectedFreeThreadsNumber = awaitingTasks.size(); //There is no reason to keep this synchronized
            if (expectedFreeThreadsNumber == 0) {                 //as pool may drain tasks
                setDelegationState(NO_DELEGATION);
                return;
            }

            List<Node> selectedNodes = nodes.getMinHaving(expectedFreeThreadsNumber);
            if (selectedNodes.isEmpty()) {                        //No need to keep it synchronized, as it doesn't change
                setDelegationState(AWAITING_FREE_THREADS);
                return;
            }

            for (Node selectedNode : selectedNodes) {
                for (int i = 0; i < selectedNode.getAvailableThreads(); ) {
                    WorkerPoolTask task = (WorkerPoolTask) awaitingTasks.poll();
                    if (task == null) {                           //Synchronizing it doesn't change anything.
                        setDelegationState(NO_DELEGATION);
                        return;
                    }
                    if (delegateTask(selectedNode, task))
                        i++;
                }
            }

            synchronized (this) {
                if (delegationState == DURING_DELEGATION) { // No more tasks and free threads appeared
                    delegationState = NO_DELEGATION;
                    return;
                }
                //current state is DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION
                delegationState = DURING_DELEGATION;
            }
        }
    }

    private synchronized void setDelegationState(TaskDelegationState delegationState) {
        this.delegationState = delegationState;
    }

    /**
     * Delegate single task to other node.
     *
     * @return true if delegation succeeded, it may fail if task is not registered in system.
     */
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

    enum TaskDelegationState {
        NO_DELEGATION,
        DURING_DELEGATION,
        DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION,
        AWAITING_FREE_THREADS;
    }
}
