package pl.edu.uj.cluster;

import pl.edu.uj.cluster.messages.PrimaryHeartBeat;
import pl.edu.uj.engine.CancelJarJobsEvent;
import pl.edu.uj.engine.TaskCancelledEvent;
import pl.edu.uj.engine.TaskFinishedEvent;
import pl.edu.uj.engine.workerpool.WorkerPoolOverflowEvent;

import java.util.stream.Stream;

/**
 * Created by alanhawrot on 29.02.2016.
 */
public interface Distributor {

    void on(WorkerPoolOverflowEvent event);

    void onTaskDelegation(ExternalTask externalTask);

    void onRedirect(String currentNodeId, String destinationNodeId, long taskId);

    void onSry(String nodeId, long taskId);

    void onTaskFinished(TaskFinishedEvent event);

    void onTaskExecutionCompleted(long taskId, Object taskResult);

    void onTaskExecutionCompleted(long taskId, Throwable exception);

    void on(CancelJarJobsEvent event);

    void on(TaskCancelledEvent event);

    void onNodeGone(String nodeId);

    void onNewNode(String newNodeId);

    void onCancelJarJobs(String sourceNodeId, String jarPath);

    void onPrimaryHeartBeat(String sourceNodeId, PrimaryHeartBeat primaryHeartBeat);

    void onJarRequest(String nodeId, String jarFileName);

    void onJarDelivery(String nodeId, String jarFileName, byte[] jar);

}
