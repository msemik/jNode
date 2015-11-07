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

	public Future<TaskResult> submitTask(Task task) throws InterruptedException, ExecutionException {
		logger.info("Task is executed");

		Future<TaskResult> taskResult = taskExecutor.submit(task);
		return taskResult;
	}

	public boolean isInactiveWorker() {
		return taskExecutor.getPoolSize() - taskExecutor.getActiveCount() > 0;
	}

	@EventListener
	public void onApplicationShutdown(ApplicationShutdownEvent e) {
		taskExecutor.shutdown();
	}
}
