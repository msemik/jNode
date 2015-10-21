package pl.uj.edu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import sun.rmi.runtime.Log;

@SpringBootApplication
public class JNodeApplication {
    @Autowired
    private DependencyInjectionTest diTest;

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(JNodeApplication.class, args);
        JNodeApplication jNodeApplication = applicationContext.getBean(JNodeApplication.class);
        jNodeApplication.diTest.sayHello(args);

    }

    @Component
    private static class DependencyInjectionTest {
        Logger logger = LoggerFactory.getLogger(JNodeApplication.class);

        public void sayHello(String[] args) {
            logger.error("hello world, args:" + String.join(" ", args));
        }
    }
}
