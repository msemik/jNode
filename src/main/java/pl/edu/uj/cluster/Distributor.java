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

    void onRedirect(ExternalTask externalTask, String destinationNodeId);

    void onSry(ExternalTask externalTask);

    void onTaskFinished(TaskFinishedEvent event);

    void onTaskExecutionCompleted(ExternalTask externalTask, Object taskResult);

    void onTaskExecutionCompleted(ExternalTask externalTask, Throwable exception);

    void onCancelJarJobs(CancelJarJobsEvent event);

    void onTaskCancelled(TaskCancelledEvent event);

    void onNodeGone(NodeGoneEvent event);
}
