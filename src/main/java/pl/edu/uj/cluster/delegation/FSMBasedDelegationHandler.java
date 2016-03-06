package pl.edu.uj.cluster.delegation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.uj.cluster.MessageGateway;
import pl.edu.uj.cluster.message.Redirect;
import pl.edu.uj.cluster.message.Sry;
import pl.edu.uj.cluster.message.TaskDelegation;
import pl.edu.uj.cluster.node.Node;
import pl.edu.uj.cluster.node.Nodes;
import pl.edu.uj.cluster.task.DelegatedTask;
import pl.edu.uj.cluster.task.DelegatedTaskRegistry;
import pl.edu.uj.cluster.task.ExternalTask;
import pl.edu.uj.cluster.task.ExternalTaskRegistry;
import pl.edu.uj.engine.workerpool.WorkerPool;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Optional.of;
import static pl.edu.uj.cluster.delegation.DefaultTaskDelegationEvent.*;
import static pl.edu.uj.cluster.delegation.DefaultState.*;


@Component
public class FSMBasedDelegationHandler implements DelegationHandler {
    private Logger logger = LoggerFactory.getLogger(FSMBasedDelegationHandler.class);
    private AtomicReference<State> delegationState = new AtomicReference<>(NO_DELEGATION);
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

    @Override
    public void handleDuringOnWorkerPoolEvent() {
        changeStateAndExecuteEventAction(OVERFLOW);
    }

    @Override
    public void handleDuringOnHeartBeat() {
        changeStateAndExecuteEventAction(HEARTBEAT);
    }

    /**
     * Simulates Finite State Machine,
     * for given pair event, state:
     * - change to appropriate state
     * - execute related action.
     * <p/>
     * <p/>
     * Transitions table:
     * GIVEN				WHEN				CHANGE STATE TO			AND EXECUTE
     * <p/>
     * NO_DELEGATION		OVERFLOW			DURING_DELEGATION		delegateTasks()
     * NO_DELEGATION		PRIMARY				NO_DELEGATION			empty()
     * NO_DELEGATION		DELEGATION_FINISHED	NO_DELEGATION			error()
     * NO_DELEGATION		NO_THREADS			NO_DELEGATION			error()
     * DURING_DELEGATION	OVERFLOW			SCHEDULED_RE_EXECUTE	empty()
     * DURING_DELEGATION	PRIMARY				SCHEDULED_RE_EXECUTE	empty()
     * DURING_DELEGATION	DELEGATION_FINISHED	NO_DELEGATION			empty()
     * DURING_DELEGATION	NO_THREADS			AWAITING_THREADS		empty()
     * SCHEDULED_RE_EXECUTE	OVERFLOW			SCHEDULED_RE_EXECUTE	empty()
     * SCHEDULED_RE_EXECUTE	PRIMARY				SCHEDULED_RE_EXECUTE	empty()
     * SCHEDULED_RE_EXECUTE	DELEGATION_FINISHED	DURING_DELEGATION		delegateTasks()
     * SCHEDULED_RE_EXECUTE	NO_THREADS			AWAITING_THREADS		empty()
     * AWAITING_THREADS		OVERFLOW			AWAITING_THREADS		empty()
     * AWAITING_THREADS		PRIMARY				DURING_DELEGATION		delegateTasks()
     * AWAITING_THREADS		DELEGATION_FINISHED	AWAITING_THREADS		empty()
     * AWAITING_THREADS		NO_THREADS			AWAITING_THREADS		error()
     */
    private void changeStateAndExecuteEventAction(DelegationEvent event) {
        State prevState = null;
        State nextState = null;
        try {
            while (true) {
                do {
                    prevState = delegationState.get();
                    nextState = event.nextState(this, prevState);

                } while (!delegationState.compareAndSet(prevState, nextState));

                //Executing action after atomically changed state
                //Action may produce another action.
                Optional<DelegationEvent> nextEvent = event.executeAction(this, prevState);
                if (nextEvent.isPresent()) {
                    event = nextEvent.get();
                } else {
                    return;
                }
            }
        } catch (Throwable t) {
            logger.error(t + " during state transition from " + prevState + " to " + nextState);
        }
    }

    public Optional<DelegationEvent> empty() {
        return empty();
    }

    public Optional<DelegationEvent> error() {
        throw new AssertionError("Illegal transition appeared");
    }

    public Optional<DelegationEvent> delegateTasks() {
        logger.debug("task delegation started");

        while (true) {
            Optional<Node> selectedNode;
            Optional<WorkerPoolTask> task = workerPool.pollTask();

            if (!task.isPresent()) {
                return of(TASK_DELEGATION_FINISHED);
            }

            synchronized (nodes) { // Synchronization with HeartBeat nodes.updateAfterHeartBeat method.
                selectedNode = nodes.drainThreadFromNodeHavingHighestPriority();
                if (!selectedNode.isPresent()) {
                    workerPool.submitTask(task.get());
                    changeStateAndExecuteEventAction(NO_THREADS);
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

}
