package pl.edu.uj.jnode.cluster.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import pl.edu.uj.jnode.jarpath.Jar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public synchronized Set<ExternalTask> removeAll(Jar jar) {
        Set<ExternalTask> removedExternalTasks = new HashSet<>();
        set.forEach(externalTask -> {
            if (externalTask.belongToJar(jar)) {
                removedExternalTasks.add(externalTask);
            }
        });
        set.removeAll(removedExternalTasks);
        return removedExternalTasks;
    }

    public List<Long> getTaskIds() {
        return set.stream().map(ExternalTask::getTaskId).collect(Collectors.toList());
    }
}
