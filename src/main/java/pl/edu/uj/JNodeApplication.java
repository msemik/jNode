package pl.edu.uj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pl.edu.uj.options.OptionsEventsDispatcher;

@Configuration
@EnableAspectJAutoProxy
@EnableSpringConfigured
@ComponentScan
public class JNodeApplication {

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(JNodeApplication.class);
        JNodeApplication jNodeApplication = applicationContext.getBean(JNodeApplication.class);

        OptionsEventsDispatcher optionsEventsDispatcher = applicationContext.getBean(OptionsEventsDispatcher.class);
        optionsEventsDispatcher.dispatchOptionsEvents(args);

        Logger logger = LoggerFactory.getLogger(JNodeApplication.class);
        logger.info("jNode application has started, args:" + String.join(" ", args));


    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        taskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors());

        return taskExecutor;
    }

}
