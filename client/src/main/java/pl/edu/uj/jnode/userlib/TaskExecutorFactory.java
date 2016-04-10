package pl.edu.uj.jnode.userlib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by alanhawrot on 15.11.2015.
 */
public class TaskExecutorFactory {
    public static TaskExecutor createTaskExecutor() {
        return new UserTaskReceiver();
    }

    private static class UserTaskReceiver implements TaskExecutor {
        private Class<?> userTaskReceiverClass;
        private Object userTaskReceiverInstance;

        public UserTaskReceiver() {
            try {
                userTaskReceiverClass = ClassLoader.getSystemClassLoader().loadClass("pl.edu.uj.jnode.engine.DefaultTaskReceiver");
                userTaskReceiverInstance = userTaskReceiverClass.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        public void doAsync(Task task, Callback callback) {
            try {
                Method doAsyncMethod = userTaskReceiverClass.getMethod("doAsync", Object.class, Object.class);
                doAsyncMethod.invoke(userTaskReceiverInstance, task, callback);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
