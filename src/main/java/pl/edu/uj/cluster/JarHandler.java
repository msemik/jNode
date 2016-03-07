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
import pl.edu.uj.engine.event.CancelJarJobsEvent;
import pl.edu.uj.engine.event.TaskCancelledEvent;
import pl.edu.uj.engine.workerpool.WorkerPool;
import pl.edu.uj.jarpath.JarPathManager;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public void onTaskDelegation(ExternalTask task) {
        String sourceNodeId = task.getSourceNodeId();
        Path jarName = task.getJarName();
        if (!jarPathManager.hasJar(sourceNodeId, jarName))
            synchronized (this) {
                if (!jarPathManager.hasJar(sourceNodeId, jarName)) {
                    if (notRequestedYet(jarName)) {
                        messageGateway.send(new JarRequest(jarName.toString()));
                    }
                    awaitingForJarExternalTasks.add(task);
                    return;
                }
            }
        workerPool.submitTask(task);
    }

    private synchronized boolean notRequestedYet(Path jarName) {
        return !awaitingForJarExternalTasks
                .stream()
                .filter(task -> task.getJarName().equals(jarName))
                .findAny()
                .isPresent();
    }

    public synchronized void onJarDelivery(String nodeId, String jarFileName, byte[] jar) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(jar);
        Path jarFilePath = Paths.get(jarFileName);
        jarPathManager.storeJarWithProperties(jarFilePath, inputStream, nodeId);
        Iterator<ExternalTask> it = awaitingForJarExternalTasks.iterator();
        while (it.hasNext()) {
            ExternalTask task = it.next();
            if (task.belongToJar(jarFilePath)) {
                it.remove();
                workerPool.submitTask(task);
            }
        }
    }

    @EventListener
    public synchronized void on(CancelJarJobsEvent event) {
        Iterator<ExternalTask> it = awaitingForJarExternalTasks.iterator();
        Path jarFileName = event.getJarFileName();
        while (it.hasNext()) {
            ExternalTask task = it.next();
            if (task.belongToJar(jarFileName)) {
                it.remove();
                eventPublisher.publishEvent(new TaskCancelledEvent(this, task, event.getOrigin()));
            }
        }
    }

    public void onJarRequest(String nodeId, String jarFileName) {
        byte[] jar = jarPathManager.readJarContent(Paths.get(jarFileName));
        if (jar.length == 0) {
            logger.warn("jar content missing for " + jarFileName);
            return;
        }
        messageGateway.send(new JarDelivery(jar, jarFileName), nodeId);
    }
}
