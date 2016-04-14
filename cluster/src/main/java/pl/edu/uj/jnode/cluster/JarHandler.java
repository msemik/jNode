package pl.edu.uj.jnode.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import pl.edu.uj.jnode.cluster.task.ExternalTask;
import pl.edu.uj.jnode.cluster.task.TaskService;
import pl.edu.uj.jnode.engine.EmptyCallback;
import pl.edu.uj.jnode.engine.event.CancelJarJobsEvent;
import pl.edu.uj.jnode.engine.event.TaskReceivedEvent;
import pl.edu.uj.jnode.engine.workerpool.WorkerPool;
import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.jarpath.JarFactory;
import pl.edu.uj.jnode.jarpath.JarPathManager;

import java.io.ByteArrayInputStream;
import java.util.List;

@Component
public class JarHandler {
    private Logger logger = LoggerFactory.getLogger(JarHandler.class);
    private LinkedMultiValueMap awaitingForJarExternalTasks = new LinkedMultiValueMap<>();
    @Autowired
    private JarPathManager jarPathManager;
    @Autowired
    private WorkerPool workerPool;
    @Autowired
    private MessageGateway messageGateway;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private JarFactory jarFactory;
    @Autowired
    private TaskService taskService;

    public void onTaskDelegation(ExternalTask task) {
        Jar jar = jarFactory.getFor(task.getSourceNodeId(), task.getJarName());
        if (!jar.isValidExistingJar()) {
            synchronized (this) {
                if (!jar.isValidExistingJar()) {
                    if (notRequestedYet(jar)) {
                        taskService.jarRequest(jar);
                    }
                    awaitingForJarExternalTasks.add(jar, task);
                    return;
                }
            }
        }
        task.deserialize(jar);
        publishTaskReceivedEvent(task);
    }

    private boolean notRequestedYet(Jar jar) {
        return !awaitingForJarExternalTasks.containsKey(jar);
    }

    private void publishTaskReceivedEvent(ExternalTask task) {
        eventPublisher.publishEvent(new TaskReceivedEvent(this, task, EmptyCallback.INSTANCE));
    }

    public void onJarDelivery(String nodeId, String fileName, byte[] jarContent) {
        logger.debug(fileName + " delivery from " + nodeId);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(jarContent);
        Jar jar = jarFactory.getFor(nodeId, fileName);
        jar.storeJarContent(inputStream);
        jar.storeDefaultProperties();

        List<ExternalTask> awaitingTasks;
        synchronized (this) {
            awaitingTasks = awaitingForJarExternalTasks.remove(jar);
        }

        if (awaitingTasks == null) {
            return;
        }

        for (ExternalTask task : awaitingTasks) {
            task.deserialize(jar);
            publishTaskReceivedEvent(task);
        }
    }

    @EventListener
    public synchronized void on(CancelJarJobsEvent event) {
        Jar jar = event.getJar();
        awaitingForJarExternalTasks.remove(jar);
    }

    public void onJarRequest(String requesterNodeId, String fileName) {
        logger.debug(fileName + " requested from " + requesterNodeId);
        Jar jar = jarFactory.getFor(fileName);
        byte[] jarContent = jar.readContent();
        if (jarContent.length == 0) {
            logger.warn("jar content missing for " + jar);
            return;
        }
        taskService.jarDelivery(requesterNodeId, fileName, jarContent);
    }
}
