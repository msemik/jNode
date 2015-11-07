package pl.uj.edu.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

@Component
public class CallbackStorage {
	private Map<Task, Callback> taskCallbackHashMap = new ConcurrentHashMap<>();
	
	public void putIfAbsent(Task task, Callback callback) {
		taskCallbackHashMap.putIfAbsent(task, callback);
	}
	
	public Callback remove(Task task) {
		return taskCallbackHashMap.remove(task);
	}
}
