package pl.edu.uj.jnode.engine.workerpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.crosscuting.ReflectionUtils;
import pl.edu.uj.jnode.jarpath.Jar;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static java.util.Collections.emptyList;

/**
 * Created by michal on 22.11.15.
 */
@Component
public class ExecutingTasks {
    private Map<Jar, List<Future<Object>>> futureMap = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(ExecutingTasks.class);
    private String ids;

    public synchronized void put(Jar jar, Future<Object> future) {
        List<Future<Object>> futures = futureMap.getOrDefault(jar, new ArrayList<>());
        futures.add(future);
        futureMap.putIfAbsent(jar, futures);
    }

    public synchronized boolean remove(Jar jar, Future<Object> future) {
        List<Future<Object>> futures = futureMap.getOrDefault(jar, emptyList());
        boolean result = futures.removeIf(f -> f == future);
        if (futures.isEmpty()) {
            futureMap.remove(jar);
        }
        return result;
    }

    @Override
    public String toString() {
        return "ExecutingTasks" + futureMap;
    }

    public synchronized int size() {
        return (int) futureMap.values().stream().flatMap(l -> l.stream()).count();
    }

    public synchronized int cancelAllRunningJobs(Jar jar) {
        int cancelledJobs = 0;
        List<Future<Object>> futures = removeAll(jar);
        logger.debug("Cancelling " + futures.size() + " tasks for " + jar + " all tasks: " + futures.size());
        for (Future<Object> future : futures) {
            if (!future.isCancelled() && !future.isDone()) {
                if (!future.cancel(true)) {
                    logger.warn("Task couldn't be cancelled:" + future);
                } else {
                    ++cancelledJobs;
                }
            }
        }
        return cancelledJobs;
    }

    private List<Future<Object>> removeAll(Jar jar) {
        List<Future<Object>> removedItems = futureMap.remove(jar);
        if (removedItems == null) {
            return emptyList();
        }
        return removedItems;
    }

    public synchronized void remove(Future<Callable> future) {
        Iterator<Map.Entry<Jar, List<Future<Object>>>> it = futureMap.entrySet().iterator();
        while (it.hasNext()) {
            List<Future<Object>> futures = it.next().getValue();
            if (futures.remove(future)) {
                return;
            }
        }
        logger.error("No future task found");
    }

    public synchronized List<WorkerPoolTask> getTasks() {
        List<WorkerPoolTask> tasks = new ArrayList<>();
        futureMap.values().forEach(l -> l.forEach(f -> tasks.add(getTaskFromFuture(f))));
        return tasks;
    }

    private WorkerPoolTask getTaskFromFuture(Future<Object> f) {
        return (WorkerPoolTask) ReflectionUtils.readFieldValue(FutureTask.class, f, "callable");
    }
}
