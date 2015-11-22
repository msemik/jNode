package pl.edu.uj.engine.workerpool;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Created by michal on 22.11.15.
 */
@Component
public class ExecutingTasks {
    private Map<Path, List<Future<Object>>> futureMap = new HashMap<>();

    public synchronized void put(Path jarName, Future<Object> future) {
        List<Future<Object>> futures = futureMap.getOrDefault(jarName, new ArrayList<>());
        futures.add(future);
        futureMap.putIfAbsent(jarName, futures);
    }

    public synchronized boolean remove(Path path, Future<Object> future) {
        List<Future<Object>> futures = futureMap.get(path);
        return futures.remove(future);
    }

    public synchronized List<Future<Object>> removeAll(Path path) {
        List<Future<Object>> removedItems = futureMap.remove(path);
        if (removedItems == null)
            return Collections.emptyList();
        return removedItems;
    }

    @Override
    public String toString() {
        return "ExecutingTasks" + futureMap;
    }
}
