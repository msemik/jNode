package pl.uj.edu.engine;

import org.springframework.stereotype.Component;
import pl.uj.edu.userlib.Task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by alanhawrot on 18.11.2015.
 */
@Component
public class TaskJarRegistry {

    private Map<Task, String> map = new ConcurrentHashMap<>();

    public void putIfAbsent(Task task, String jarName) {
        map.putIfAbsent(task, jarName);
    }

    public String get(Task task) {
        return map.get(task);
    }

    public String remove(Task task) {
        return map.remove(task);
    }
}
