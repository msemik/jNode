package pl.uj.edu.engine;

import java.util.HashMap;

import org.springframework.stereotype.Component;

import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

@Component
public class CallbackStorage {
	
	private HashMap<Task, Callback> taskCallbackHashMap = new HashMap<>();
	
	public void putIfAbsent(Task task, Callback callback) {
		taskCallbackHashMap.putIfAbsent(task, callback);
	}
	
	public Callback remove(Task task) {
		return taskCallbackHashMap.remove(task);
	}
}
