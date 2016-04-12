package pl.edu.uj.jnode.cluster.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.edu.uj.jnode.cluster.MessageGateway;
import pl.edu.uj.jnode.cluster.callback.SerializableCallbackWrapper;
import pl.edu.uj.jnode.cluster.message.CancelJarJobs;
import pl.edu.uj.jnode.cluster.message.JarDelivery;
import pl.edu.uj.jnode.cluster.message.JarRequest;
import pl.edu.uj.jnode.cluster.message.Redirect;
import pl.edu.uj.jnode.cluster.message.RegisterDelegatedSubTask;
import pl.edu.uj.jnode.cluster.message.Sry;
import pl.edu.uj.jnode.cluster.message.TaskDelegation;
import pl.edu.uj.jnode.cluster.message.TaskExecutionCompleted;
import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;
import pl.edu.uj.jnode.jarpath.Jar;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TaskService {
    private Logger logger = LoggerFactory.getLogger(TaskService.class);
    @Autowired
    private DelegatedTaskRegistry delegatedTaskRegistry;
    @Autowired
    private ExternalTaskRegistry externalTaskRegistry;
    @Autowired
    private MessageGateway messageGateway;

    public void delegateTask(WorkerPoolTask task, String destinationNodeId) {
        DelegatedTask delegatedTask = new DelegatedTask(task, destinationNodeId);
        delegatedTaskRegistry.add(delegatedTask);

        ExternalTask externalTask = new ExternalTask(task, messageGateway.getCurrentNodeId());
        messageGateway.send(new TaskDelegation(externalTask), destinationNodeId);
    }

    public void redirectTask(ExternalTask externalTask, String destinationNodeId) {
        messageGateway.send(new Redirect(destinationNodeId, externalTask.getTaskId()), externalTask.getSourceNodeId());
    }

    public void sry(String destinationNodeId, String taskId) {
        messageGateway.send(new Sry(taskId), destinationNodeId);
    }

    public void cancelJarJobs(String nodeId, Jar jar) {
        messageGateway.send(new CancelJarJobs(jar.getFileNameAsString()), nodeId);
    }

    public Stream<String> getNodeIds(Set<DelegatedTask> delegatedTasks) {
        return delegatedTasks.stream().map(DelegatedTask::getDestinationNodeId).distinct();
    }

    public void taskExecutionCompleted(ExternalTask task, Object taskResult) {
        messageGateway.send(new TaskExecutionCompleted(taskResult, task.getTaskId()), task.getSourceNodeId());
    }

    public List<WorkerPoolTask> unwrapTasks(Collection<DelegatedTask> delegatedTasks) {
        return delegatedTasks.stream().map(DelegatedTask::getTask).collect(Collectors.toList());
    }

    public void jarDelivery(String requesterNodeId, String fileName, byte[] jarContent) {
        messageGateway.send(new JarDelivery(jarContent, fileName), requesterNodeId);
    }

    public void jarRequest(Jar jar) {
        messageGateway.send(new JarRequest(jar.getFileNameAsString()), jar.getNodeId());
    }

    public void registerDelegatedSubTask(ExternalTask externalTask, SerializableCallbackWrapper callback, String nodeId) {
        messageGateway.send(new RegisterDelegatedSubTask(externalTask, callback), nodeId);
    }
}
