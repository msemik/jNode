package pl.edu.uj.jnode.cluster.delegation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.cluster.node.Node;
import pl.edu.uj.jnode.cluster.node.Nodes;
import pl.edu.uj.jnode.cluster.task.DelegatedTaskRegistry;
import pl.edu.uj.jnode.cluster.task.ExternalTask;
import pl.edu.uj.jnode.cluster.task.ExternalTaskRegistry;
import pl.edu.uj.jnode.cluster.task.TaskService;
import pl.edu.uj.jnode.engine.workerpool.WorkerPool;
import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static pl.edu.uj.jnode.cluster.delegation.SimpleDelegationHandler.State.*;

/**
 * GIVEN					WHEN				CHANGE STATE TO			AND EXECUTE NO_DELEGATION			OVERFLOW			DURING_DELEGATION		delegateTasks()
 * NO_DELEGATION			PRIMARY				NO_DELEGATION			empty() DURING_DELEGATION		OVERFLOW			SCHEDULED_RE_EXECUTE	empty()
 * DURING_DELEGATION		PRIMARY				SCHEDULED_RE_EXECUTE	empty() SCHEDULED_RE_EXECUTE
 * OVERFLOW			SCHEDULED_RE_EXECUTE	empty() SCHEDULED_RE_EXECUTE	    PRIMARY				SCHEDULED_RE_EXECUTE	empty()
 * AWAITING_THREADS		    OVERFLOW			AWAITING_THREADS		empty() AWAITING_THREADS
 * PRIMARY				DURING_DELEGATION		delegateTasks()
 */
@Component
public class SimpleDelegationHandler implements DelegationHandler {
    @Autowired
    TaskService taskService;
    private Logger logger = LoggerFactory.getLogger(SimpleDelegationHandler.class);
    private AtomicReference<State> state = new AtomicReference<>(NO_DELEGATION);
    @Autowired
    private Nodes nodes;
    @Autowired
    private WorkerPool workerPool;
    @Autowired
    private ExternalTaskRegistry externalTaskRegistry;
    @Autowired
    private DelegatedTaskRegistry delegatedTaskRegistry;

    public void handleDuringOnWorkerPoolEvent() {
        logger.info("handleDuringOnWorkerPoolEvent");
        while (true) {
            State prevState = state.get();
            State nextState = prevState;
            if (prevState == NO_DELEGATION) {
                nextState = DURING_DELEGATION;
            } else if (prevState == DURING_DELEGATION) {
                nextState = DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION;
            }
            if (state.compareAndSet(prevState, nextState)) {
                logger.debug("changed from " + prevState + " to " + nextState);
                if (prevState == NO_DELEGATION) {
                    delegateTasks();
                }
                return;
            }
            logger.debug("state change missed in handleDuringOnWorkerPoolEvent");
        }
    }

    public void handleDuringOnHeartBeat() {
        logger.info("handleDuringOnHeartBeat");
        while (true) {
            State prevState = state.get();
            State nextState = prevState;
            if (prevState == DURING_DELEGATION) {
                //New threads may produce more delegations
                nextState = DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION;
            } else if (prevState == AWAITING_FREE_THREADS) {
                //Delegations are waiting but there is no free threads
                nextState = DURING_DELEGATION;
            }
            if (state.compareAndSet(prevState, nextState)) {
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
     */
    private void delegateTasks() {
        logger.debug("task delegation started");

        untilTasksAndThreadsAreAvailable:
        while (true) {
            Optional<Node> selectedNode;
            Optional<WorkerPoolTask> task = workerPool.pollTask();

            if (!task.isPresent()) {
                while (true) {
                    State prevState = state.get();
                    State nextState;
                    if (prevState == DURING_DELEGATION) {
                        // No more tasks and free threads appeared
                        nextState = NO_DELEGATION;
                    } else {
                        //prevState == DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION
                        nextState = DURING_DELEGATION;
                    }
                    if (state.compareAndSet(prevState, nextState)) {
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

            synchronized (nodes) { // Synchronization with HeartBeat nodes.updateAfterHeartBeat method.
                selectedNode = nodes.drainThreadFromNodeHavingHighestPriority();
                if (!selectedNode.isPresent()) {
                    workerPool.submitTask(task.get(), true);
                    State nextState = AWAITING_FREE_THREADS;
                    State prevState = state.getAndSet(nextState);
                    logger.debug("changed from " + prevState + " to " + nextState);
                    return;
                }
            }

            if (!delegateTask(selectedNode.get(), task.get())) {
                //If delegation failed we release the thread to keep proper number of free threads up-to-date
                //If HeartBeat happened between draining a thread and returning it, this operation will take no effect
                //as we are working on invalidated version of node.
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
        String taskId = task.getTaskId();
        if (task.isExternal()) {
            ExternalTask externalTask = (ExternalTask) task;
            if (!externalTaskRegistry.remove(externalTask)) {
                logger.info("There is no entry for given " + externalTask);
                logger.info("Not sending any Sry or Redirect message for " + externalTask);
                return false;
            }

            if (externalTask.isOriginatedAt(destinationNode)) {
                taskService.sry(destinationNodeId, taskId);
            } else {
                taskService.redirectTask(externalTask, destinationNodeId);
            }
        } else {
            taskService.delegateTask(task, destinationNodeId);
        }
        return true;
    }

    enum State {
        NO_DELEGATION,
        DURING_DELEGATION,
        DURING_DELEGATION_WITH_SCHEDULED_RE_EXECUTION,
        AWAITING_FREE_THREADS;
    }
}
