package pl.uj.edu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import pl.uj.edu.options.OptionsEventsDispatcher;


@Configuration
@ComponentScan
public class JNodeApplication {

    @Autowired
    private DependencyInjectionTest diTest;

    public static void main(String[] args) {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(JNodeApplication.class);
        JNodeApplication jNodeApplication = applicationContext.getBean(JNodeApplication.class);

        OptionsEventsDispatcher optionsEventsDispatcher = applicationContext.getBean(OptionsEventsDispatcher.class);
        optionsEventsDispatcher.dispatchOptionsEvents(args);
        jNodeApplication.diTest.sayHello(args);
    }


    @Component
    private static class DependencyInjectionTest {
        Logger logger = LoggerFactory.getLogger(JNodeApplication.class);

        public void sayHello(String[] args) {
            logger.error("hellą world, args:" + String.join(" ", args));
        }
    }
}
