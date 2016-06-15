package pl.edu.uj.jnode.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.edu.uj.jnode.main.options.OptionsEventsDispatcher;

import java.util.concurrent.*;

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
    private volatile boolean isShutDown = false;

    public static void main(String[] args) {
        JNodeApplication.args = args;
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(JNodeApplication.class);
        applicationContext.getBean(JNodeApplication.class).initialize();
    }

    public void initialize() {
        OptionsEventsDispatcher optionsEventsDispatcher = applicationContext.getBean(OptionsEventsDispatcher.class);
        if (!optionsEventsDispatcher.dispatchOptionsEvents(args)) {
            return;
        }
        if(isShutDown)
            return;
        applicationContext.publishEvent(new OptionsDispatchedEvent(applicationContext));
        applicationContext.publishEvent(new ApplicationInitializedEvent(applicationContext));
        isInitialized = true;
        logger.info("jNode application has started, args:" + String.join(" ", args));
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    @EventListener
    public void on(ApplicationShutdownEvent event) {
        isShutDown = true;
        delayedApplicationContextShutdown();
    }

    private void delayedApplicationContextShutdown() {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (applicationContext instanceof ConfigurableApplicationContext) {
                ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) this.applicationContext;
                if (applicationContext.isActive()) {
                    applicationContext.close();
                }
            } else {
                logger.error("invalid application context class: " + applicationContext.getClass().getCanonicalName());
            }
        });
    }

    public boolean isShutDown() {
        return isShutDown;
    }
}
