package pl.uj.edu.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.uj.edu.jarpath.JarDeletedEvent;
import pl.uj.edu.jarpath.JarStateChangedEvent;

import java.nio.file.Path;

@Component
public class JarCoordinator {
    private Logger logger = LoggerFactory.getLogger(JarCoordinator.class);

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JarEventLoopQueueRegistry jarEventLoopQueueRegistry;

    @EventListener
    public void onJarStateChanged(JarStateChangedEvent event) {
        Path path = event.getPath();
        logger.info("Got jar " + path + " with properties " + event.getProperties()
                + " perhaps we can start a job if executionState is not started?");

        String jarName = path.toFile().getName();

        EventLoopThread eventLoopThread = context.getBean(EventLoopThread.class);
        jarEventLoopQueueRegistry.putIfAbsent(jarName, eventLoopThread.getEventLoopQueue());
        eventLoopThread.start();

        JarLauncher loader = new JarLauncher(path);
        loader.launchMain();
    }

    @EventListener
    public void onJarDeleted(JarDeletedEvent event) {
        logger.info("Deleted jar " + event.getJarPath() + " we may removed job if exists");
    }
}
