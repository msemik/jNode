package pl.edu.uj.cluster.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import pl.edu.uj.cluster.Distributor;
import pl.edu.uj.cluster.ExternalTask;
import pl.edu.uj.cluster.JGroups;
import pl.edu.uj.cluster.MessageGateway;
import pl.edu.uj.engine.CancelJarJobsEvent;
import pl.edu.uj.engine.TaskCancelledEvent;
import pl.edu.uj.engine.TaskFinishedEvent;
import pl.edu.uj.engine.workerpool.WorkerPoolOverflowEvent;

import javax.annotation.PostConstruct;

@Component
public class EmptyDistributor implements Distributor {

    @Autowired
    private MessageGateway messageGateway;

    @Override
    public void onWorkerPoolOverflow(WorkerPoolOverflowEvent event) {

    }

    @Override
    public void onTaskDelegation(ExternalTask externalTask) {

    }

    @Override
    public void onRedirect(String currentNodeId, String destinationNodeId, long taskId) {

    }

    @Override
    public void onSry(String nodeId, long taskId) {

    }

    @Override
    public void onTaskFinished(TaskFinishedEvent event) {

    }

    @Override
    public void onTaskExecutionCompleted(long taskId, Object taskResult) {

    }

    @Override
    public void onTaskExecutionCompleted(long taskId, Throwable exception) {

    }

    @Override
    public void onCancelJarJobs(CancelJarJobsEvent event) {

    }

    @Override
    public void onTaskCancelled(TaskCancelledEvent event) {

    }

    @Override
    public void onNodeGone(String nodeId) {

    }

    @Override
    public void onCancelJarJobs(String sourceNodeId, String jarPath) {

    }

    @Override
    public void onPrimaryHeartBeat(String sourceNodeId, PrimaryHeartBeat primaryHeartBeat) {

    }

    @Override
    public void onJarRequest(String nodeId, String jarFileName) {

    }

    @Override
    public void onJarDelivery(String nodeId, String jarFileName, byte[] jar) {

    }
}
