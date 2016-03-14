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
import pl.edu.uj.cluster.task.*;
import pl.edu.uj.engine.event.CancelJarJobsEvent;
import pl.edu.uj.engine.event.TaskCancelledEvent;
import pl.edu.uj.engine.event.TaskFinishedEvent;
import pl.edu.uj.engine.workerpool.WorkerPool;
import pl.edu.uj.engine.workerpool.WorkerPoolOverflowEvent;
import pl.edu.uj.jarpath.JarPathManager;

import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static pl.edu.uj.engine.event.CancellationEventOrigin.EXTERNAL;
import static pl.edu.uj.engine.event.CancellationEventOrigin.INTERNAL;

/**
 * Created by alanhawrot on 01.03.2016.
 */
@Component
public class DefaultDistributor implements Distributor {
    @Autowired
    JarHandler jarHandler;
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
    private DelegationHandler delegationHandler;
    @Autowired
    private HeartBeatHandler heartBeatHandler;
    @Autowired
    private Nodes nodes;
    @Autowired
    private NodeFactory nodeFactory;
    @Autowired
    private TaskService taskService;
    @Autowired
    private JarPathManager jarPathManager;

    @Override
    @EventListener
    public void on(WorkerPoolOverflowEvent event) {
        delegationHandler.handleDuringOnWorkerPoolEvent();
    }

    @Override
    public void onTaskDelegation(ExternalTask externalTask) {
        if (externalTaskRegistry.add(externalTask)) {
            jarHandler.onTaskDelegation(externalTask);
        } else {
            logger.debug("Task with taskId: " + externalTask.getTaskId() + " is already in registry");
        }
    }

    @Override
    public void onRedirect(String currentNodeId, String destinationNodeId, long taskId) {
        Optional<DelegatedTask> delegatedTask = delegatedTaskRegistry.remove(taskId);
        if (!delegatedTask.isPresent()) {
            logger.debug("Task absent in registry, currentNodeId: " + currentNodeId + ", taskId: " + taskId);
            return;
        }
        delegatedTask.get().incrementPriority();
        if (workerPool.hasAvailableThreads()) {
            workerPool.submitTask(delegatedTask.get().getTask());
        } else {
            taskService.delegateTask(delegatedTask.get().getTask(), destinationNodeId);
        }
    }

    @Override
    public void onSry(String nodeId, long taskId) {
        Set<DelegatedTask> delegatedTasks = delegatedTaskRegistry.removeAll(nodeId);
        if (!delegatedTasks.isEmpty()) {
            logger.debug("Task absent in registry, nodeId " + nodeId + ", taskId " + taskId);
            return;
        }
        if (delegatedTasks.size() > 1) {
            logger.error("More than one delegated task found in delegatedTaskRegistry, delegated to " + nodeId + ", taskId: " + taskId);
        }
        DelegatedTask delegatedTask = delegatedTasks.iterator().next();
        delegatedTask.incrementPriority();
        workerPool.submitTask(delegatedTask.getTask());
    }

    @Override
    public void onTaskExecutionCompleted(long taskId, Object taskResultOrException) {
        Optional<DelegatedTask> delegatedTask = delegatedTaskRegistry.remove(taskId);
        if (delegatedTask.isPresent()) {
            eventPublisher.publishEvent(new TaskFinishedEvent(this, delegatedTask.get().getTask(), taskResultOrException));
        } else {
            logger.debug("Task absent in registry, taskId: " + taskId);
        }
    }

    @Override
    public void on(TaskFinishedEvent event) {
        if (!event.getTask().isExternal())
            return;
        ExternalTask externalTask = (ExternalTask) event.getTask();
        if (externalTaskRegistry.remove(externalTask)) {
            taskService.taskExecutionCompleted(externalTask, event.getTaskResultOrException());
        } else {
            logger.info("Task absent in registry, taskId: " + externalTask.getTaskId() + ", not sending the result");
        }
    }

    @EventListener
    @Override
    public void on(CancelJarJobsEvent event) {
        if (!jarPathManager.hasJar(event.getJarFileName())) {
            return;
        }
        Set<DelegatedTask> delegatedTasks = delegatedTaskRegistry.removeAll(event.getJarFileName());
        Stream<String> nodeIds = taskService.getNodeIds(delegatedTasks);
        nodeIds.forEach(nodeId -> taskService.cancelJarJobs(nodeId, event.getJarFileName()));
    }

    @Override
    @EventListener
    public void on(TaskCancelledEvent event) {
        if (!event.getTask().isExternal())
            return;
        ExternalTask task = (ExternalTask) event.getTask();
        if (event.getOrigin() == INTERNAL) {
            taskService.sry(task.getSourceNodeId(), task.getTaskId());
        }
        if (!externalTaskRegistry.remove(task)) {
            logger.debug("Task absent in registry: " + task);
        }
    }

    @Override
    public void onNodeGone(String nodeId) {
        boolean removedFromNodes = nodes.remove(nodeId);
        Set<DelegatedTask> delegatedTasks = delegatedTaskRegistry.removeAll(nodeId);
        taskService.unwrapTasks(delegatedTasks).forEach(workerPool::submitTask);
        Set<ExternalTask> externalTasks = externalTaskRegistry.removeAll(nodeId);
        sendCancelJarJobsForEachJar(externalTasks);
        logger.debug(String.join(", "
                , "onNodeGone: " + nodeId
                , "removed from nodes: " + removedFromNodes
                , "removed delegated tasks:" + delegatedTasks.size()
                , "removed external tasks:" + externalTasks.size()));
    }

    private void sendCancelJarJobsForEachJar(Set<ExternalTask> externalTasks) {
        externalTasks
                .stream()
                .map(ExternalTask::getJarName)
                .distinct()
                .forEach(jarPath -> {
                    CancelJarJobsEvent event = new CancelJarJobsEvent(this, jarPath, EXTERNAL);
                    eventPublisher.publishEvent(event);
                });
    }

    @Override
    public void onNewNode(String newNodeId) {
        Node newNode = nodeFactory.createNode(newNodeId);
        nodes.add(newNode);
        heartBeatHandler.forceOutgoing();
    }

    @Override
    public void onCancelJarJobs(String sourceNodeId, String jarPath) {
        eventPublisher.publishEvent(new CancelJarJobsEvent(this, Paths.get(jarPath), EXTERNAL));
    }

    @Override
    public void onPrimaryHeartBeat(String sourceNodeId, PrimaryHeartBeat primaryHeartBeat) {
        heartBeatHandler.handleIncoming(sourceNodeId, primaryHeartBeat);
        delegationHandler.handleDuringOnHeartBeat();
    }

    @Override
    public void onJarRequest(String nodeId, String jarFileName) {
        jarHandler.onJarRequest(nodeId, jarFileName);
    }

    @Override
    public void onJarDelivery(String nodeId, String jarFileName, byte[] jar) {
        jarHandler.onJarDelivery(nodeId, jarFileName, jar);
    }
}
