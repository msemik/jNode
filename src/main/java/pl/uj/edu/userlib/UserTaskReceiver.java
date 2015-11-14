package pl.uj.edu.userlib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class UserTaskReceiver {
    private Class<?> userTaskReceiverClass;
    private Object userTaskReceiverInstance;

    private void createUserTaskReceiver() {
        try {
            userTaskReceiverClass = ClassLoader.getSystemClassLoader().loadClass("pl.uj.edu.engine.UserTaskReceiver");
            userTaskReceiverInstance = userTaskReceiverClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public UserTaskReceiver() {
        createUserTaskReceiver();
    }

    public void doAsync(Task task, Callback callback) {
        try {
            Method doAsyncMethod = userTaskReceiverClass.getMethod("doAsync", Task.class, Callback.class);
            doAsyncMethod.invoke(userTaskReceiverInstance, task, callback);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
