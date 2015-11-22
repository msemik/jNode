package pl.uj.edu.engine.eventloop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pl.uj.edu.engine.workerpool.WorkerPoolTask;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("prototype")
public class CallbackStorage {
    private Logger logger = LoggerFactory.getLogger(CallbackStorage.class);
    private Map<WorkerPoolTask, Callback> taskCallbackMap = new ConcurrentHashMap<>();

    public void putIfAbsent(WorkerPoolTask task, Callback callback) {
        logger.debug("Callback " + callback.toString() + " has been stored");

        taskCallbackMap.putIfAbsent(task, callback);
    }

    public Callback remove(Task task) {
        Callback callback = taskCallbackMap.remove(task);

        logger.debug("Callback " + callback.toString() + " has been removed");

        return callback;
    }

    public boolean isEmpty() {
        return taskCallbackMap.isEmpty();
    }
}
