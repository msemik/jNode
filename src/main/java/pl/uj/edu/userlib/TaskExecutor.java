package pl.uj.edu.userlib;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationEventPublisher;

import pl.uj.edu.engine.NewTaskCreatedEvent;

@Configurable
public class TaskExecutor {
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	public void doAsync(Task task, Callback callback) {
		System.out.println("jNode");
		
		eventPublisher.publishEvent(new NewTaskCreatedEvent(this, task, callback));
	}
}
