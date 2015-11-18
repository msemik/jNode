package pl.uj.edu.engine;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by alanhawrot on 18.11.2015.
 */
@Component
public class JarEventLoopQueueRegistry {

    private Map<String, EventLoopQueue> map = new ConcurrentHashMap<>();

    public void putIfAbsent(String jarName, EventLoopQueue queue) {
        map.putIfAbsent(jarName, queue);
    }

    public EventLoopQueue get(String jarName) {
        return map.get(jarName);
    }

    public EventLoopQueue remove(String jarName) {
        return map.remove(jarName);
    }
}
