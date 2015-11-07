package pl.uj.edu.userlib;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationEventPublisher;

import pl.uj.edu.engine.NewTaskCreatedEvent;

/**
 * This is a version of class TaskExecutor loaded by jNode.
 * 
 * User should have a version without any dependencies and with
 * one empty (without any implementation) method:
 * public void doAsync(Task task, Callback callback) {}
 * 
 * Because build-time weaving is enabled, TaskExecutor instances
 * created with 'new' keyword in user's code will be treated
 * as beans in jNode, so autowiring will work.
 * 
 * @author alanhawrot
 */
@Configurable
public class TaskExecutor {
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	public void doAsync(Task task, Callback callback) {
		eventPublisher.publishEvent(new NewTaskCreatedEvent(this, task, callback));
	}
}
