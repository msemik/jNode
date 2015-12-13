package pl.edu.uj.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.edu.uj.ApplicationShutdownEvent;
import pl.edu.uj.engine.eventloop.EventLoopThreadRegistry;
import pl.edu.uj.engine.workerpool.WorkerPool;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by michal on 13.12.15.
 */

@Component
public class JNodeStateMonitor extends Thread {

    @Autowired
    private EventLoopThreadRegistry eventLoopThreadRegistry;

    @Autowired
    private WorkerPool workerPool;

    @PostConstruct
    public void init() {
        start();
    }

    @EventListener
    public void onApplicationShutdown(ApplicationShutdownEvent e) {
        interrupt();
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                sleep(Duration.ofSeconds(5).toMillis());
            } catch (InterruptedException e) {
                return;
            }
            StringBuilder b = new StringBuilder();
            b.append("---------\n");
            b.append("JNodeStateMonitor summary\n");
            b.append("Event loop threads: " + String.join(", ", getEventLoopThreadsPaths()) + "\n");
            b.append("Jobs in worker pool: " + workerPool.jobsInPool() + "\n");
            b.append("---------\n");
            System.out.println(b.toString());


        }
    }

    private List<String> getEventLoopThreadsPaths() {
        return eventLoopThreadRegistry.jarPaths()
                .stream()
                .map(p -> p.toString())
                .collect(Collectors.toList());
    }
}
