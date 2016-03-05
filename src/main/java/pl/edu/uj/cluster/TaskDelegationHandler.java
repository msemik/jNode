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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static pl.edu.uj.cluster.TaskDelegationHandler.TaskDelegationState.*;


@Component
public class TaskDelegationHandler {
    private Logger logger = LoggerFactory.getLogger(TaskDelegationHandler.class);
    private AtomicReference<TaskDelegationState> delegationState = new AtomicReference<>(NO_DELEGATION);
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

    /* State transitions:

     * NO_DELEGATION -> DURING DELEGATION (start task delegation in this case)
     * DURING DELEGATION -> DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION
     * AWAITING_FREE_THREADS -> AWAITING_FREE_THREADS
     * DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION -> DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION
     **/
    public void handleDuringOnWorkerPoolEvent() {
        while (true) {
            TaskDelegationState prevState = delegationState.get();
            TaskDelegationState nextState = prevState;
            if (prevState == NO_DELEGATION) {
                nextState = DURING_DELEGATION;
            } else if (prevState == DURING_DELEGATION) {
                nextState = DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION;
            }
            if (delegationState.compareAndSet(prevState, nextState)) {
                logger.debug("changed from " + prevState + " to " + nextState);
                if (prevState == NO_DELEGATION) {
                    delegateTasks();
                }
                return;
            }
            logger.debug("state change missed in handleDuringOnWorkerPoolEvent");
        }
    }

    /**
     * State transitions:
     *
     * NO_DELEGATION -> NO_DELEGATION
     * DURING DELEGATION -> DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION
     * AWAITING_FREE_THREADS -> DURING_DELEGATION (start task delegation in this case)
     * DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION -> DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION
     */
    public void handleDuringOnHeartBeat() {
        while (true) {
            TaskDelegationState prevState = delegationState.get();
            TaskDelegationState nextState = prevState;
            if (prevState == DURING_DELEGATION) {
                //New threads may produce more delegations
                nextState = DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION;
            } else if (prevState == AWAITING_FREE_THREADS) {
                //Delegations are waiting but there is no free threads
                nextState = DURING_DELEGATION;
            }
            if (delegationState.compareAndSet(prevState, nextState)) {
                logger.debug("changed from " + prevState + " to " + nextState);
                if (prevState == AWAITING_FREE_THREADS) {
                    delegateTasks();
                }
                return;
            }
            logger.debug("state change missed in handleDuringOnHeartBeat");
        }
    }

    /**
     * Sends tasks until there is no tasks or threads left
     *
     * State transitions:
     *
     * DURING DELEGATION -> NO_DELEGATION (when there was no workerPoolOverflowEvent during last execution)
     *                   -> AWAITING_FREE_THREADS (when there is no free threads in cluster)
     * DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION -> DURING_DELEGATION (when re executing delegation)
     *                                               -> AWAITING_FREE_THREADS (when there is no free threads in cluster)
     * NO_DELEGATION -> *  can't happen
     * AWAITING_FREE_THREADS -> * can't happen
     */
    private void delegateTasks() {
        logger.debug("task delegation started");

        untilTasksAndThreadsAreAvailable:
        while (true) {
            Optional<Node> selectedNode;
            Optional<WorkerPoolTask> task = workerPool.pollTask();

            if (!task.isPresent()) {
                while (true) {
                    TaskDelegationState prevState = delegationState.get();
                    TaskDelegationState nextState;
                    if (prevState == DURING_DELEGATION) {
                        // No more tasks and free threads appeared
                        nextState = NO_DELEGATION;
                    } else {
                        //prevState == DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION
                        nextState = DURING_DELEGATION;
                    }
                    if (delegationState.compareAndSet(prevState, nextState)) {
                        logger.debug("changed from " + prevState + " to " + nextState);
                        if (prevState == DURING_DELEGATION) {
                            return;
                        } else {
                            //prevState == DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION
                            continue untilTasksAndThreadsAreAvailable;
                        }
                    }
                    logger.debug("state change missed in delegateTasks when task wasn't present");
                }
            }

            synchronized (nodes) { // Synchronization with HeartBeat nodes updateAfterHeartBeat.
                selectedNode = nodes.drainThreadFromNodeHavingHighestPriority();
                if (!selectedNode.isPresent()) {
                    workerPool.submitTask(task.get());
                    // Synchronization with worker pool overflow events

                    TaskDelegationState nextState = AWAITING_FREE_THREADS;
                    TaskDelegationState prevState = delegationState.getAndSet(nextState);
                    logger.debug("changed from " + prevState + " to " + nextState);
                    return;
                }
            }

            if (!delegateTask(selectedNode.get(), task.get())) {
                //If delegation failed we release the thread.
                //Imo its better to think there is one additional thread(which may not be true) than forgetting it.
                //Perhaps if there is no HeartBeat from other nodes we may drain all of them on unsuccessful task delegations.
                nodes.returnThread(selectedNode.get());
            }
        }
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

            if (externalTask.isOriginatedAt(destinationNode)) {
                messageGateway.send(new Sry(taskId), destinationNodeId);
            } else {
                messageGateway.send(new Redirect(destinationNodeId, taskId), externalTask.getSourceNodeId());
            }
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
