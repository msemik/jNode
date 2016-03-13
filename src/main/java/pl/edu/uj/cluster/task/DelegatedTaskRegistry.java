package pl.edu.uj.cluster.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;

/**
 * Created by alanhawrot on 01.03.2016.
 */
@Component
public class DelegatedTaskRegistry {
    private Logger logger = LoggerFactory.getLogger(DelegatedTaskRegistry.class);
    private Map<Long, DelegatedTask> map = new HashMap<>();

    public synchronized boolean add(DelegatedTask delegatedTask) {
        return map.putIfAbsent(delegatedTask.getTaskId(), delegatedTask) == null;
    }

    public synchronized boolean remove(DelegatedTask delegatedTask) {
        return map.remove(delegatedTask.getTaskId(), delegatedTask);
    }

    public synchronized Optional<DelegatedTask> remove(long taskId) {
        DelegatedTask delegatedTask = map.remove(taskId);
        return delegatedTask != null ? Optional.of(delegatedTask) : Optional.empty();
    }

    public synchronized Set<DelegatedTask> removeAll(String nodeId) {
        Set<DelegatedTask> removedDelegatedTasks = new HashSet<>();
        map.values().forEach(delegatedTask -> {
            if (delegatedTask.getDestinationNodeId().compareTo(nodeId) == 0) {
                removedDelegatedTasks.add(delegatedTask);
            }
        });
        map.values().removeAll(removedDelegatedTasks);
        return removedDelegatedTasks;
    }

    public synchronized Set<DelegatedTask> removeAll(Path jarFileName) {
        Set<DelegatedTask> removedDelegatedTasks = new HashSet<>();
        map.values().forEach(delegatedTask -> {
            if (delegatedTask.getJarName().compareTo(jarFileName) == 0) {
                removedDelegatedTasks.add(delegatedTask);
            }
        });
        map.values().removeAll(removedDelegatedTasks);
        return removedDelegatedTasks;
    }
}
