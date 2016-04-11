package pl.edu.uj.jnode.engine.workerpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import pl.edu.uj.jnode.main.options.PoolSizeOptionEvent;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Component
public class PriorityThreadPoolTaskExecutorFactory {
    private static Logger logger = LoggerFactory.getLogger(PriorityThreadPoolTaskExecutorFactory.class);
    private Optional<Integer> poolSize = empty();

    @EventListener
    public void on(PoolSizeOptionEvent event) {
        poolSize = of(event.getPoolSize());
    }

    @Bean
    @Lazy
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new PriorityThreadPoolTaskExecutor();
        int taskExecutorSize = poolSize.orElse(Runtime.getRuntime().availableProcessors());
        taskExecutor.setCorePoolSize(taskExecutorSize);
        taskExecutor.setMaxPoolSize(taskExecutorSize);
        logger.info("Creating ThreadPoolTaskExecutor with " + taskExecutorSize + " workers");
        return taskExecutor;
    }
}
