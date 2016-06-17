package pl.edu.uj.jnode.userlib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.apache.commons.lang3.reflect.MethodUtils.invokeMethod;

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

        @Override
        public void closeApp(Task preCloseAppTask) {
            try {
                Method stopAppAsyncMethod = userTaskReceiverClass.getMethod("closeAppAsync", Object.class);
                stopAppAsyncMethod.invoke(userTaskReceiverInstance, preCloseAppTask);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        @Override
        public long getAvailableWorkers() {
            return (long) invokeMethodOnUserTaskReceiver("getAvailableWorkers");
        }

        @Override
        public long getTotalWorkers() {
            return (long) invokeMethodOnUserTaskReceiver("getTotalWorkers");
        }

        @Override
        public Object getBean(Class<?> beanClass) {
            return invokeMethodOnUserTaskReceiver("getBean", beanClass);
        }

        private Object invokeMethodOnUserTaskReceiver(String methodName, Object... params) {
            try {
                return invokeMethod(userTaskReceiverInstance, methodName, params);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
