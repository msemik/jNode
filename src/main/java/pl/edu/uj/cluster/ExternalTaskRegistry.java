package pl.edu.uj.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by alanhawrot on 01.03.2016.
 */
@Component
public class ExternalTaskRegistry {
    private Logger logger = LoggerFactory.getLogger(ExternalTaskRegistry.class);
    private Set<ExternalTask> set = new HashSet<>();

    public synchronized boolean add(ExternalTask externalTask) {
        return set.add(externalTask);
    }

    public synchronized boolean remove(ExternalTask externalTask) {
        return set.remove(externalTask);
    }

    public synchronized Set<ExternalTask> removeAll(String nodeId) {
        Set<ExternalTask> removedExternalTasks = new HashSet<>();
        set.forEach(externalTask -> {
            if (externalTask.getSourceNodeId().compareTo(nodeId) == 0) {
                removedExternalTasks.add(externalTask);
            }
        });
        set.removeAll(removedExternalTasks);
        return removedExternalTasks;
    }

    public synchronized Set<ExternalTask> removeAll(Path jarFileName) {
        Set<ExternalTask> removedExternalTasks = new HashSet<>();
        set.forEach(externalTask -> {
            if (externalTask.getJarName().compareTo(jarFileName) == 0) {
                removedExternalTasks.add(externalTask);
            }
        });
        set.removeAll(removedExternalTasks);
        return removedExternalTasks;
    }
}
