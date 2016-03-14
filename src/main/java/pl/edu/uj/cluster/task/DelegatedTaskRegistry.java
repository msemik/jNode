package pl.edu.uj.cluster.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.edu.uj.jarpath.Jar;

import java.util.*;

/**
 * Created by alanhawrot on 01.03.2016.
 */
@Component
public class DelegatedTaskRegistry {
    private Logger logger = LoggerFactory.getLogger(DelegatedTaskRegistry.class);
    private Set<DelegatedTask> set = new HashSet<>();

    public synchronized boolean add(DelegatedTask delegatedTask) {
        return set.add(delegatedTask);
    }

    public synchronized boolean remove(DelegatedTask delegatedTask) {
        return set.remove(delegatedTask);
    }

    public synchronized Optional<DelegatedTask> remove(long taskId) {
        Iterator<DelegatedTask> it = set.iterator();
        while (it.hasNext()) {
            DelegatedTask delegatedTask = it.next();
            if (delegatedTask.getTaskId() == taskId) {
                it.remove();
                return Optional.of(delegatedTask);
            }
        }
        return Optional.empty();
    }

    public synchronized Set<DelegatedTask> removeAll(String nodeId) {
        Set<DelegatedTask> removedDelegatedTasks = new HashSet<>();
        set.forEach(delegatedTask -> {
            if (delegatedTask.getDestinationNodeId().compareTo(nodeId) == 0) {
                removedDelegatedTasks.add(delegatedTask);
            }
        });
        set.removeAll(removedDelegatedTasks);
        return removedDelegatedTasks;
    }

    public synchronized Set<DelegatedTask> removeAll(Jar jar) {
        Set<DelegatedTask> removedDelegatedTasks = new HashSet<>();
        set.forEach(delegatedTask -> {
            if (delegatedTask.belongToJar(jar)) {
                removedDelegatedTasks.add(delegatedTask);
            }
        });
        set.removeAll(removedDelegatedTasks);
        return removedDelegatedTasks;
    }

}
