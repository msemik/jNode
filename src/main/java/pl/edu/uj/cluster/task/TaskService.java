package pl.edu.uj.cluster.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.uj.cluster.MessageGateway;
import pl.edu.uj.cluster.delegation.FSMBasedDelegationHandler;
import pl.edu.uj.cluster.message.*;
import pl.edu.uj.engine.workerpool.WorkerPoolTask;

import java.nio.file.Path;
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

    public void sry(String destinationNodeId, int taskId) {
        messageGateway.send(new Sry(taskId), destinationNodeId);
    }

    public void cancelJarJobs(String nodeId, Path jarFileName) {
        messageGateway.send(new CancelJarJobs(jarFileName.toString()), nodeId);
    }

    public Stream<String> getNodeIds(Set<DelegatedTask> delegatedTasks) {
        return delegatedTasks
                .stream()
                .map(DelegatedTask::getDestinationNodeId)
                .distinct();
    }

    public void taskExecutionCompleted(ExternalTask task, Object taskResult) {
        messageGateway.send(new TaskExecutionCompleted(taskResult, task.getTaskId()), task.getSourceNodeId());
    }

    public List<WorkerPoolTask> unwrapTasks(Collection<DelegatedTask> delegatedTasks) {
        return delegatedTasks.stream()
                .map(DelegatedTask::getTask)
                .collect(Collectors.toList());
    }
}
