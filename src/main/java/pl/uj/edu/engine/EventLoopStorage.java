package pl.uj.edu.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.springframework.stereotype.Component;

import pl.uj.edu.userlib.Callback;
import pl.uj.edu.userlib.TaskResult;

@Component
public class EventLoopStorage {
	private Map<Callback, Future<TaskResult>> callbackResultTaskMap = new ConcurrentHashMap<>();

	public Map<Callback, Future<TaskResult>> getCallbackResultTaskMap() {
		return callbackResultTaskMap;
	}

	public void setCallbackResultTaskMap(Map<Callback, Future<TaskResult>> callbackResultTaskMap) {
		this.callbackResultTaskMap = callbackResultTaskMap;
	}	
}
