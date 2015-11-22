package pl.edu.uj.engine.eventloop;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

@Component
public class EventLoopThreadRegistry {

    private Map<Path, EventLoopThread> map = new ConcurrentHashMap<>();

    public Optional<EventLoopThread> forJarName(Path jarName) {
        return ofNullable(map.get(jarName));
    }

    public void register(Path jarName, EventLoopThread eventLoopThread) {
        map.put(jarName, eventLoopThread);
    }

    public EventLoopThread unregister(Path jarName) {
        EventLoopThread eventLoopThread = map.remove(jarName);
        return eventLoopThread;
    }

    @Override
    public String toString() {
        return "EventLoopThreadRegistry{" + map + '}';
    }
}
