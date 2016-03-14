package pl.edu.uj.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.cluster.message.JarDelivery;
import pl.edu.uj.cluster.message.JarRequest;
import pl.edu.uj.cluster.task.ExternalTask;
import pl.edu.uj.engine.EmptyCallback;
import pl.edu.uj.engine.event.CancelJarJobsEvent;
import pl.edu.uj.engine.event.NewTaskReceivedEvent;
import pl.edu.uj.engine.event.TaskCancelledEvent;
import pl.edu.uj.engine.workerpool.WorkerPool;
import pl.edu.uj.jarpath.Jar;
import pl.edu.uj.jarpath.JarFactory;
import pl.edu.uj.jarpath.JarPathManager;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class JarHandler {
    private Logger logger = LoggerFactory.getLogger(JarHandler.class);
    private List<ExternalTask> awaitingForJarExternalTasks = new ArrayList<>();
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

    public void onTaskDelegation(ExternalTask task) {
        Jar jar = task.getJar();
        if (!jar.isValidExistingJar())
            synchronized (this) {
                if (!jar.isValidExistingJar()) {
                    if (notRequestedYet(jar)) {
                        messageGateway.send(new JarRequest(jar.getFileNameAsString()));
                    }
                    awaitingForJarExternalTasks.add(task);
                    return;
                }
            }
        task.deserialize(jar);
        publishTaskReceivedEvent(task);
    }

    private void publishTaskReceivedEvent(ExternalTask task) {
        eventPublisher.publishEvent(new NewTaskReceivedEvent(this, task, new EmptyCallback()));
    }

    private synchronized boolean notRequestedYet(Jar jar) {
        return !awaitingForJarExternalTasks
                .stream()
                .filter(task -> task.belongToJar(jar))
                .findAny()
                .isPresent();
    }

    public synchronized void onJarDelivery(String nodeId, String fileName, byte[] jarContent) {
        logger.debug(fileName + " delivery from " + nodeId);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(jarContent);
        Jar jar = jarFactory.getFor(nodeId, fileName);
        jar.storeDefaultProperties();
        jar.storeJarContent(inputStream);
        Iterator<ExternalTask> it = awaitingForJarExternalTasks.iterator();
        while (it.hasNext()) {
            ExternalTask task = it.next();
            if (task.belongToJar(jar)) {
                it.remove();
                task.deserialize(jar);
                publishTaskReceivedEvent(task);
            }
        }
    }

    @EventListener
    public synchronized void on(CancelJarJobsEvent event) {
        Iterator<ExternalTask> it = awaitingForJarExternalTasks.iterator();
        Jar jar = event.getJar();
        while (it.hasNext()) {
            ExternalTask task = it.next();
            if (task.belongToJar(jar)) {
                it.remove();
                eventPublisher.publishEvent(new TaskCancelledEvent(this, task, event.getOrigin()));
            }
        }
    }

    public void onJarRequest(String nodeId, String fileName) {
        logger.debug(fileName + " requested from " + nodeId);
        Jar jar = jarFactory.getFor(nodeId, fileName);
        byte[] jarContent = jar.readContent();
        if (jarContent.length == 0) {
            logger.warn("jar content missing for " + jar);
            return;
        }
        messageGateway.send(new JarDelivery(jarContent, fileName), nodeId);
    }
}