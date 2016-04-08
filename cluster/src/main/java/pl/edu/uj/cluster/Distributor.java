package pl.edu.uj.cluster;

import pl.edu.uj.cluster.message.PrimaryHeartBeat;
import pl.edu.uj.cluster.task.ExternalTask;
import pl.edu.uj.engine.events.CancelJarJobsEvent;
import pl.edu.uj.engine.events.TaskCancelledEvent;
import pl.edu.uj.engine.events.TaskFinishedEvent;
import pl.edu.uj.engine.workerpool.WorkerPoolOverflowEvent;

/**
 * Created by alanhawrot on 29.02.2016.
 */
public interface Distributor {
    void on(WorkerPoolOverflowEvent event);

    void onTaskDelegation(ExternalTask externalTask);

    void onRedirect(String currentNodeId, String destinationNodeId, long taskId);

    void onSry(String nodeId, long taskId);

    void onTaskExecutionCompleted(long taskId, Object taskResult);

    void on(TaskFinishedEvent event);

    void on(CancelJarJobsEvent event);

    void on(TaskCancelledEvent event);

    void onNodeGone(String nodeId);

    void onNewNode(String newNodeId);

    void onCancelJarJobs(String sourceNodeId, String jarPath);

    void onPrimaryHeartBeat(String sourceNodeId, PrimaryHeartBeat primaryHeartBeat);

    void onJarRequest(String nodeId, String jar);

    void onJarDelivery(String nodeId, String jarFileName, byte[] jar);
}