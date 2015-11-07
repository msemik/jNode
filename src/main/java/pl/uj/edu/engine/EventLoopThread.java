package pl.uj.edu.engine;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import pl.uj.edu.ApplicationShutdownEvent;

@Component
public class EventLoopThread extends Thread {
	private static final long SCAN_PERIOD = 1000; // 1 second

	private boolean shutdown = false;

	Logger logger = LoggerFactory.getLogger(EventLoopThread.class);

	@Autowired
	private EventLoopStorage eventLoopStorage;

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

				if (!eventLoopStorage.getCallbackResultTaskMap().isEmpty()) {
					eventLoopStorage.getCallbackResultTaskMap().forEach((callback, futureResultTask) -> {
						if (futureResultTask.isDone()) {
							try {
								callback.doCallback(futureResultTask.get());
								eventLoopStorage.getCallbackResultTaskMap().remove(callback);

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
