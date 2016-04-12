package pl.edu.uj.jnode.cluster.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import pl.edu.uj.jnode.jarpath.Jar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by alanhawrot on 01.03.2016.
 */
@Component
public class DelegatedTaskRegistry {
    private Logger logger = LoggerFactory.getLogger(DelegatedTaskRegistry.class);
    private Map<String, DelegatedTask> map = new HashMap<>();

    public synchronized boolean add(DelegatedTask delegatedTask) {
        return map.putIfAbsent(delegatedTask.getTaskId(), delegatedTask) == null;
    }

    public synchronized Optional<DelegatedTask> remove(String taskId) {
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

    public synchronized Set<DelegatedTask> removeAll(Jar jar) {
        Set<DelegatedTask> removedDelegatedTasks = new HashSet<>();
        map.values().forEach(delegatedTask -> {
            if (delegatedTask.belongToJar(jar)) {
                removedDelegatedTasks.add(delegatedTask);
            }
        });
        map.values().removeAll(removedDelegatedTasks);
        return removedDelegatedTasks;
    }

    public synchronized List<String> getTaskIds() {
        return map.values().stream().map(DelegatedTask::getTaskId).collect(Collectors.toList());
    }
}
