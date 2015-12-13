package pl.edu.uj.engine.workerpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;

import static java.util.Collections.emptyList;

/**
 * Created by michal on 22.11.15.
 */
@Component
public class ExecutingTasks {
    private Map<Path, List<Future<Object>>> futureMap = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(ExecutingTasks.class);

    public synchronized void put(Path jarName, Future<Object> future) {
        List<Future<Object>> futures = futureMap.getOrDefault(jarName, new ArrayList<>());
        futures.add(future);
        futureMap.putIfAbsent(jarName, futures);
    }

    public synchronized boolean remove(Path path, Future<Object> future) {
        List<Future<Object>> futures = futureMap.getOrDefault(path, emptyList());
        boolean result = futures.removeIf(f -> f == future);
        if(futures.isEmpty())
            futureMap.remove(path);
        return result;
    }

    private List<Future<Object>> removeAll(Path path) {
        List<Future<Object>> removedItems = futureMap.remove(path);
        if (removedItems == null)
            return emptyList();
        return removedItems;
    }

    @Override
    public String toString() {
        return "ExecutingTasks" + futureMap;
    }

    public synchronized int size() {
        return (int) futureMap.values().stream().flatMap(l -> l.stream()).count();
    }

    public synchronized int cancelAllRunningJobs(Path fileName) {
        int cancelledJobs = 0;
        List<Future<Object>> futures = removeAll(fileName);
        logger.debug("Cancelling " + futures.size() + " tasks for " + fileName + " all tasks: " + futures.size());
        for (Future<Object> future : futures) {
            if (!future.isCancelled() && !future.isDone())
                if (!future.cancel(true))
                    logger.warn("Task couldn't be cancelled:" + future);
                else
                    ++cancelledJobs;
        }
        return cancelledJobs;
    }
}
