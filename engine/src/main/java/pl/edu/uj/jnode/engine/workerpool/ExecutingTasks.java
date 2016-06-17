package pl.edu.uj.jnode.engine.workerpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.crosscuting.ReflectionUtils;
import pl.edu.uj.jnode.jarpath.Jar;

import java.io.Serializable;
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
    private Map<Jar, List<Future<Serializable>>> futureMap = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(ExecutingTasks.class);

    public synchronized void put(Jar jar, Future<Serializable> future) {
        List<Future<Serializable>> futures = futureMap.getOrDefault(jar, new ArrayList<>());
        futures.add(future);
        futureMap.putIfAbsent(jar, futures);
    }

    public synchronized boolean remove(Jar jar, Future<Serializable> future) {
        List<Future<Serializable>> futures = futureMap.getOrDefault(jar, emptyList());
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
        List<Future<Serializable>> futures = removeAll(jar);
        logger.debug("Cancelling " + futures.size() + " tasks for " + jar + " all tasks: " + futures.size());
        for (Future<Serializable> future : futures) {
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

    private List<Future<Serializable>> removeAll(Jar jar) {
        List<Future<Serializable>> removedItems = futureMap.remove(jar);
        if (removedItems == null) {
            return emptyList();
        }
        return removedItems;
    }

    public synchronized void remove(Future<Callable> future) {
        Iterator<Map.Entry<Jar, List<Future<Serializable>>>> it = futureMap.entrySet().iterator();
        while (it.hasNext()) {
            List<Future<Serializable>> futures = it.next().getValue();
            if (futures.remove(future)) {
                return;
            }
        }
        logger.debug("No future task found");
    }

    public synchronized List<WorkerPoolTask> getTasks() {
        List<WorkerPoolTask> tasks = new ArrayList<>();
        futureMap.values().forEach(l -> l.forEach(f -> tasks.add(getTaskFromFuture(f))));
        return tasks;
    }

    private WorkerPoolTask getTaskFromFuture(Future<Serializable> f) {
        return (WorkerPoolTask) ReflectionUtils.readFieldValue(FutureTask.class, f, "callable");
    }
}
