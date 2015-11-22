package pl.uj.edu.userlib;

import pl.uj.edu.engine.workerpool.WorkerPoolTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by alanhawrot on 15.11.2015.
 */
public class TaskReceiverFactory {

    public static TaskReceiver createTaskReceiver() {
        return new UserTaskReceiver();
    }

    private static class UserTaskReceiver implements TaskReceiver {
        private Class<?> userTaskReceiverClass;
        private Object userTaskReceiverInstance;

        public UserTaskReceiver() {
            try {
                userTaskReceiverClass = ClassLoader.getSystemClassLoader().loadClass("pl.uj.edu.engine.DefaultTaskReceiver");
                userTaskReceiverInstance = userTaskReceiverClass.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        public void doAsync(WorkerPoolTask task, Callback callback) {
            try {
                Method doAsyncMethod = userTaskReceiverClass.getMethod("doAsync", Object.class, Object.class);
                doAsyncMethod.invoke(userTaskReceiverInstance, task, callback);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
