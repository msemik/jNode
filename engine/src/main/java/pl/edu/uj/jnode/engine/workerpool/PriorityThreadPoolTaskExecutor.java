package pl.edu.uj.jnode.engine.workerpool;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by alanhawrot on 13.03.2016.
 */
public class PriorityThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {
    private static final int QUEUE_CAPACITY = 11; // as it is in the docs

    @Override
    protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
        Comparator<Runnable> priorityComparator = (o1, o2) -> {
            if (o1 instanceof WorkerPoolTask && o2 instanceof WorkerPoolTask) {
                WorkerPoolTask task1 = (WorkerPoolTask) o1;
                WorkerPoolTask task2 = (WorkerPoolTask) o2;
                return task1.getPriority() < task2.getPriority() ? 1 : task1.getPriority() == task2.getPriority() ? 0 : -1;
            }
            return 0;
        };

        return queueCapacity > 1 && queueCapacity < Integer.MAX_VALUE ? new PriorityBlockingQueue<>(queueCapacity, priorityComparator)
                : new PriorityBlockingQueue<>(QUEUE_CAPACITY, priorityComparator);
    }
}
