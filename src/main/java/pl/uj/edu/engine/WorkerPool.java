package pl.uj.edu.engine;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import pl.uj.edu.ApplicationShutdownEvent;
import pl.uj.edu.userlib.Task;
import pl.uj.edu.userlib.TaskResult;

/**
 * Created by michal on 31.10.15.
 */
@Component
public class WorkerPool {
	Logger logger = LoggerFactory.getLogger(WorkerPool.class);

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;
	
	@Autowired
	private CallbackStorage callbackStorage;
	
	@Autowired
	private EventLoopStorage eventLoopStorage;

	public void submitTask(Task task) throws InterruptedException, ExecutionException {
		logger.info("Task is being executed");

		Future<TaskResult> taskResult = taskExecutor.submit(task);
		
		eventLoopStorage.getCallbackResultTaskMap().putIfAbsent(callbackStorage.remove(task), taskResult);
	}

	public boolean isInactiveWorker() {
		return taskExecutor.getMaxPoolSize() - taskExecutor.getActiveCount() > 0;
	}

	@EventListener
	public void onApplicationShutdown(ApplicationShutdownEvent e) {
		taskExecutor.shutdown();
	}
}
