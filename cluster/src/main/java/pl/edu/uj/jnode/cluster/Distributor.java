package pl.edu.uj.jnode.cluster;

import pl.edu.uj.jnode.cluster.callback.SerializableCallbackWrapper;
import pl.edu.uj.jnode.cluster.message.PrimaryHeartBeat;
import pl.edu.uj.jnode.cluster.task.ExternalTask;
import pl.edu.uj.jnode.cluster.task.SerializableTaskResultWrapper;
import pl.edu.uj.jnode.engine.event.CancelJarJobsEvent;
import pl.edu.uj.jnode.engine.event.CloseAppTaskReceivedEvent;
import pl.edu.uj.jnode.engine.event.ExternalSubTaskReceivedEvent;
import pl.edu.uj.jnode.engine.event.TaskFinishedEvent;
import pl.edu.uj.jnode.engine.workerpool.WorkerPoolOverflowEvent;

/**
 * Created by alanhawrot on 29.02.2016.
 */
public interface Distributor {
    void on(WorkerPoolOverflowEvent event);

    void onTaskDelegation(ExternalTask externalTask);

    void onRedirect(String currentNodeId, String destinationNodeId, String taskId);

    void onSry(String nodeId, String taskId);

    void onTaskExecutionCompleted(String taskId, SerializableTaskResultWrapper taskResultWrapper);

    void on(TaskFinishedEvent event);

    void on(CancelJarJobsEvent event);

    void on(ExternalSubTaskReceivedEvent event);

    void onRegisterDelegatedSubTask(String sourceNodeId, ExternalTask externalTask, SerializableCallbackWrapper callback);

    void on(CloseAppTaskReceivedEvent event);

    void onCloseApp(String sourceNodeId, ExternalTask externalTask);

    void onNodeGone(String nodeId);

    void onNewNode(String newNodeId);

    void onCancelJarJobs(String sourceNodeId, String jarPath);

    void onPrimaryHeartBeat(String sourceNodeId, PrimaryHeartBeat primaryHeartBeat);

    void onJarRequest(String nodeId, String jar);

    void onJarDelivery(String nodeId, String jarFileName, byte[] jar);
}
