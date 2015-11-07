package pl.uj.edu.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.Task;

@Component
public class CallbackStorage {
	Logger logger = LoggerFactory.getLogger(CallbackStorage.class);
	
	private Map<Task, Callback> taskCallbackHashMap = new ConcurrentHashMap<>();
	
	public void putIfAbsent(Task task, Callback callback) {
		logger.info("Callback has been stored");
		
		taskCallbackHashMap.putIfAbsent(task, callback);
	}
	
	public Callback remove(Task task) {
		logger.info("Callback has been removed");
		
		return taskCallbackHashMap.remove(task);
	}
}
