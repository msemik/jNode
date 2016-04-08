package pl.edu.uj.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.edu.uj.cluster.node.Nodes;
import pl.edu.uj.cluster.task.DelegatedTaskRegistry;
import pl.edu.uj.cluster.task.ExternalTaskRegistry;
import pl.edu.uj.engine.eventloop.EventLoopThreadRegistry;
import pl.edu.uj.engine.workerpool.WorkerPool;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by michal on 13.12.15.
 */
@Component
public class JNodeStateMonitor {
    public static final int TEN_SECONDS_IN_MILLIS = 10000;
    public static final int THREE_SECONDS_IN_MILLIS = 3000;
    @Autowired
    private EventLoopThreadRegistry eventLoopThreadRegistry;
    @Autowired
    private WorkerPool workerPool;
    @Autowired
    private Nodes nodes;
    @Autowired
    private ExternalTaskRegistry externalTaskRegistry;
    @Autowired
    private DelegatedTaskRegistry delegatedTaskRegistry;

    @Scheduled(initialDelay = THREE_SECONDS_IN_MILLIS, fixedDelay = TEN_SECONDS_IN_MILLIS)
    public void run() {
        List<String> jars = getJars();
        StringBuilder b = new StringBuilder();
        b.append("EventLoopThreads: " + String.join(", ", jars) + "\n");
        b.append("Jobs in pool: " + workerPool.jobsInPool() + ", ids: " + workerPool.getIdsOfTasksInPool() + "\n");
        b.append("nodes:\n" + nodes + "\n");
        b.append("externalTaskRegistry: " + externalTaskRegistry.getTaskIds() + "\n");
        b.append("delegatedTaskRegistry: " + delegatedTaskRegistry.getTaskIds() + "\n");
        System.out.println(b.toString());
    }

    private List<String> getJars() {
        return eventLoopThreadRegistry.getJars().stream().map(Object::toString).collect(Collectors.toList());
    }
}
