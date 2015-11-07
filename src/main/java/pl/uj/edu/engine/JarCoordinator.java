package pl.uj.edu.engine;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import pl.uj.edu.jarpath.JarDeletedEvent;
import pl.uj.edu.jarpath.JarStateChangedEvent;

@Component
public class JarCoordinator {
	Logger logger = LoggerFactory.getLogger(JarCoordinator.class);
	
	@EventListener
	public void onJarStateChanged(JarStateChangedEvent event) {
		Path path = event.getPath();
		logger.error("Got jar " + path + " with properties " + event.getProperties()
				+ " perhaps we can start a job if executionState is not started?");

		JarLauncher loader = new JarLauncher(path);
		loader.launchMain();
	}

	@EventListener
	public void onJarDeleted(JarDeletedEvent event) {
		logger.error("Deleted jar " + event.getJarPath() + " we may removed job if exists");
	}
}
