package pl.uj.edu.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CallbackStorage {
    private Logger logger = LoggerFactory.getLogger(CallbackStorage.class);
    private Map<Task, Callback> taskCallbackMap = new ConcurrentHashMap<>();

    public void putIfAbsent(Task task, Callback callback) {
        logger.info("Callback " + callback.toString() + " has been stored");

        taskCallbackMap.putIfAbsent(task, callback);
    }

    public Callback remove(Task task) {
        Callback callback = taskCallbackMap.remove(task);

        logger.info("Callback " + callback.toString() + " has been removed");

        return callback;
    }
}
