package pl.uj.edu.engine;

import org.springframework.context.ApplicationEvent;

import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

public class NewTaskCreatedEvent extends ApplicationEvent {
	
	private Task task;
	private Callback callback;

	public NewTaskCreatedEvent(Object source, Task task, Callback callback) {
		super(source);
		this.task = task;
		this.callback = callback;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Callback getCallback() {
		return callback;
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}
}
