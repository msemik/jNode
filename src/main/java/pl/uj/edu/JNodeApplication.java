package pl.uj.edu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import pl.uj.edu.options.OptionsEventsDispatcher;

@Configuration
@EnableAspectJAutoProxy
@EnableSpringConfigured
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
			logger.info("jNode application has started, args:" + String.join(" ", args));
		}
	}
	
	@Bean
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
		taskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors());
		
		return taskExecutor;
	}
}
