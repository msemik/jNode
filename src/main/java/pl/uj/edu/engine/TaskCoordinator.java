package pl.uj.edu.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

@Component
public class TaskCoordinator {
    private Logger logger = LoggerFactory.getLogger(TaskCoordinator.class);

    @Autowired
    private WorkerPool workerPool;

    @Autowired
    private CallbackStorage callbackStorage;

    @EventListener
    public void onNewTaskCreated(NewTaskReceivedEvent event) {
        Task task = event.getTask();
        Callback callback = event.getCallback();

        logger.info("New task has been received: " + task);

        callbackStorage.putIfAbsent(task, callback);
        workerPool.submitTask(task);
    }
}
