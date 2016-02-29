package pl.edu.uj.cluster;

import org.springframework.context.event.EventListener;
import pl.edu.uj.engine.TaskFinishedEvent;
import pl.edu.uj.engine.workerpool.WorkerPoolOverflowEvent;

/**
 * Created by alanhawrot on 29.02.2016.
 */
public interface Distributor {

    @EventListener
    void onWorkerPoolOverflow(WorkerPoolOverflowEvent event);

    void onTaskDelegation(ExternalTask externalTask);

    void onRedirect(ExternalTask externalTask, String destinationNodeId);

    void onSry(ExternalTask externalTask);

    @EventListener
    void onTaskFinished(TaskFinishedEvent event);

    void onTaskExecutionCompleted(ExternalTask externalTask, Object taskResult);

    void onTaskExecutionCompleted(ExternalTask externalTask, Throwable exception);
}
