package pl.uj.edu.engine;

import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import pl.uj.edu.ApplicationShutdownEvent;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

@Component
public class TaskCoordinatorThread extends Thread {
	private static final long SCAN_PERIOD = 1000; // 1 second

	private boolean shutdown = false;

	Logger logger = LoggerFactory.getLogger(TaskCoordinatorThread.class);

	@Autowired
	private WorkerPool workerPool;

	@Autowired
	private TaskQueue taskQueue;
	
	@Autowired
	private CallbackStorage callbackStorage;
	
	@PostConstruct
	public void startThread() {
		start();
	}

	@EventListener
	public void onApplicationShutdown(ApplicationShutdownEvent e) {
		shutdown = true;
	}
	
	@EventListener
	public void onNewTaskCreated(NewTaskCreatedEvent event) {
		logger.info("New task has been created");
		
		Task task = event.getTask();
		Callback callback = event.getCallback();
		
		callbackStorage.putIfAbsent(task, callback);
		taskQueue.offer(task);
	}

	@Override
	public void run() {
		logger.info("TaskCoordinator is observing TaskQueue");
		
		while (true) {
			try {
				if (shutdown)
					return;

				if (!taskQueue.isEmpty() && workerPool.hasInactiveWorker()) {
					workerPool.submitTask(taskQueue.poll());
				}

				Thread.sleep(SCAN_PERIOD);
			} catch (InterruptedException | ExecutionException e) {
				logger.error(e.getMessage());
			}
		}
	}
}
