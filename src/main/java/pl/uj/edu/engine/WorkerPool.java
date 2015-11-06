package pl.uj.edu.engine;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import pl.uj.edu.userlib.Task;
import pl.uj.edu.userlib.TaskResult;

/**
 * Created by michal on 31.10.15.
 */
@Component
public class WorkerPool {
    Logger logger = LoggerFactory.getLogger(WorkerPool.class);
    
    private ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    
    public TaskResult submitTask(Task task) throws InterruptedException, ExecutionException {
    	Future<TaskResult> taskResult = taskExecutor.submit(task);
    	return taskResult.get();
    }
}
