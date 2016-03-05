package pl.edu.uj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pl.edu.uj.options.OptionsEventsDispatcher;
import pl.edu.uj.options.PoolSizeOptionEvent;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Configuration
@EnableAspectJAutoProxy
@EnableSpringConfigured
@ComponentScan
public class JNodeApplication {
    static Logger logger = LoggerFactory.getLogger(JNodeApplication.class);

    private Optional<Integer> poolSize = empty();

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(JNodeApplication.class);
        JNodeApplication jNodeApplication = applicationContext.getBean(JNodeApplication.class);

        OptionsEventsDispatcher optionsEventsDispatcher = applicationContext.getBean(OptionsEventsDispatcher.class);
        optionsEventsDispatcher.dispatchOptionsEvents(args);

        logger.info("jNode application has started, args:" + String.join(" ", args));


    }

    @EventListener
    public void on(PoolSizeOptionEvent event) {
        poolSize = of(event.getPoolSize());
    }

    @Bean
    @Lazy
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        int taskExecutorSize = poolSize.orElse(Runtime.getRuntime().availableProcessors());
        taskExecutor.setCorePoolSize(taskExecutorSize);
        taskExecutor.setMaxPoolSize(taskExecutorSize);
        logger.info("Creating ThreadPoolTaskExecutor with " + taskExecutorSize + " workers");
        return taskExecutor;
    }

}
