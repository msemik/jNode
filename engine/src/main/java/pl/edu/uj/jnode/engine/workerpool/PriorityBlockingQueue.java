package pl.edu.uj.jnode.engine.workerpool;

import pl.edu.uj.jnode.crosscuting.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by alanhawrot on 17.06.2016.
 */
public class PriorityBlockingQueue extends java.util.concurrent.PriorityBlockingQueue<Runnable> {
    public PriorityBlockingQueue(int initialCapacity, Comparator<? super Runnable> comparator) {
        super(initialCapacity, comparator);
    }

    public Runnable pollNotClosingAppTask() {
        ReentrantLock lock = null;
        try {
            lock = getLock();
            lock.lock();

            Iterator<Runnable> iterator = iterator();
            while (iterator.hasNext()) {
                Runnable item = iterator.next();
                if (!(item instanceof FutureTask)) {
                    String message = "Unexpected object type pulled from executors queue: " + item.getClass().getCanonicalName();
                    throw new AssertionError(message);
                }
                FutureTask<Callable> futureTask = (FutureTask<Callable>) item;
                WorkerPoolTask task = (WorkerPoolTask) ReflectionUtils.readFieldValue(FutureTask.class, futureTask, "callable");
                if (task instanceof CloseAppTask) {
                    iterator.remove();
                    return item;
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
        return null;
    }

    private ReentrantLock getLock() throws NoSuchFieldException, IllegalAccessException {
        Field lockField = java.util.concurrent.PriorityBlockingQueue.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        ReentrantLock lock = (ReentrantLock) lockField.get(this);
        lockField.setAccessible(false);
        return lock;
    }
}
