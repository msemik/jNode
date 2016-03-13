package pl.edu.uj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pl.edu.uj.engine.workerpool.PriorityThreadPoolTaskExecutor;
import pl.edu.uj.options.OptionsEventsDispatcher;
import pl.edu.uj.options.PoolSizeOptionEvent;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Configuration
@EnableAspectJAutoProxy
@EnableSpringConfigured
@ComponentScan
@EnableScheduling()
public class JNodeApplication {
    private static Logger logger = LoggerFactory.getLogger(JNodeApplication.class);
    private static String[] args;

    @Autowired
    private ApplicationContext applicationContext;
    private Optional<Integer> poolSize = empty();
    private volatile boolean isInitialized = false;

    public static void main(String[] args) {
        JNodeApplication.args = args;
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(JNodeApplication.class);
        applicationContext.getBean(JNodeApplication.class)
                .initialize();
    }

    public void initialize() {
        OptionsEventsDispatcher optionsEventsDispatcher = applicationContext.getBean(OptionsEventsDispatcher.class);
        optionsEventsDispatcher.dispatchOptionsEvents(args);

        applicationContext.publishEvent(new ApplicationInitializedEvent(applicationContext));
        isInitialized = true;
        logger.info("jNode application has started, args:" + String.join(" ", args));
    }

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

    public boolean isInitialized() {
        return isInitialized;
    }
}
