package pl.uj.edu.engine;

import java.util.Map;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import pl.uj.edu.ApplicationShutdownEvent;
import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.TaskResult;

@Component
public class EventLoopThread extends Thread {
	private static final long SCAN_PERIOD = 1000; // 1 second

	private boolean shutdown = false;

	Logger logger = LoggerFactory.getLogger(EventLoopThread.class);

	@Autowired
	private Map<Callback, Future<TaskResult>> eventLoopStorage;

	@PostConstruct
	public void startThread() {
		start();
	}

	@EventListener
	public void onApplicationShutdown(ApplicationShutdownEvent e) {
		shutdown = true;
	}

	@Override
	public void run() {
		logger.info("EventLoopThread is waiting for finished tasks");

		while (true) {
			try {
				if (shutdown)
					return;

				if (!eventLoopStorage.isEmpty()) {
					eventLoopStorage.forEach((callback, futureResultTask) -> {
						if (futureResultTask.isDone()) {
							try {
								callback.onSuccess(futureResultTask.get());
								eventLoopStorage.remove(callback);

								logger.info("Callback has been finished");
							} catch (Exception e) {
								logger.error(e.getMessage());
							}
						}
					});
				}

				Thread.sleep(SCAN_PERIOD);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
	}
}
