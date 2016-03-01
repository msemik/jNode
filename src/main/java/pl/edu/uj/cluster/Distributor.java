package pl.edu.uj.cluster;

import pl.edu.uj.engine.CancelJarJobsEvent;
import pl.edu.uj.engine.TaskCancelledEvent;
import pl.edu.uj.engine.TaskFinishedEvent;
import pl.edu.uj.engine.workerpool.WorkerPoolOverflowEvent;

/**
 * Created by alanhawrot on 29.02.2016.
 */
public interface Distributor {

    void onWorkerPoolOverflow(WorkerPoolOverflowEvent event);

    void onTaskDelegation(ExternalTask externalTask);

    void onRedirect(String currentNodeId, String destinationNodeId, long taskId);

    void onSry(String nodeId, long taskId);

    void onTaskFinished(TaskFinishedEvent event);

    void onTaskExecutionCompleted(long taskId, Object taskResult);

    void onTaskExecutionCompleted(long taskId, Throwable exception);

    void onCancelJarJobs(CancelJarJobsEvent event);

    void onTaskCancelled(TaskCancelledEvent event);

    void onNodeGone(String nodeId);
}
