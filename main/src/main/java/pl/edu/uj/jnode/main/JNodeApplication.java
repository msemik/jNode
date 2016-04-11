package pl.edu.uj.jnode.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.scheduling.annotation.EnableScheduling;

import pl.edu.uj.jnode.main.options.OptionsEventsDispatcher;

@Configuration
@EnableAspectJAutoProxy
@EnableSpringConfigured
@ComponentScan("pl.edu.uj.jnode")
@EnableScheduling()
public class JNodeApplication {
    private static Logger logger = LoggerFactory.getLogger(JNodeApplication.class);
    private static String[] args;
    @Autowired
    private ApplicationContext applicationContext;
    private volatile boolean isInitialized = false;

    public static void main(String[] args) {
        JNodeApplication.args = args;
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(JNodeApplication.class);
        applicationContext.getBean(JNodeApplication.class).initialize();
    }

    public void initialize() {
        OptionsEventsDispatcher optionsEventsDispatcher = applicationContext.getBean(OptionsEventsDispatcher.class);
        optionsEventsDispatcher.dispatchOptionsEvents(args);

        applicationContext.publishEvent(new OptionsDispatchedEvent(applicationContext));
        applicationContext.publishEvent(new ApplicationInitializedEvent(applicationContext));
        isInitialized = true;
        logger.info("jNode application has started, args:" + String.join(" ", args));
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}
