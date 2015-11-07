package pl.uj.edu.engine;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import pl.uj.edu.userlib.Task;

@Component
public class TaskQueue {
	Logger logger = LoggerFactory.getLogger(TaskQueue.class);
	
	private Queue<Task> queue = new ConcurrentLinkedQueue<>();
	
	public boolean offer(Task task) {
		logger.info("Task has been added to queue");
		return queue.offer(task);
	}

	public Task poll() {
		logger.info("Task has been removed from queue");
		return queue.poll();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
}
