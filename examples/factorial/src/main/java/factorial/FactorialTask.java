package factorial;

import pl.edu.uj.jnode.userlib.Task;
import pl.edu.uj.jnode.userlib.TaskExecutor;
import pl.edu.uj.jnode.userlib.TaskExecutorFactory;

import java.math.BigInteger;

/**
 * Created by alanhawrot on 13.04.2016.
 */
public class FactorialTask implements Task {
    private int begin;
    private int end;

    public FactorialTask(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    @Override
    public Object call() throws Exception {
        TaskExecutor taskExecutor = TaskExecutorFactory.createTaskExecutor();
        while (end - begin > 50) {
            int mid = (begin + end) / 2;
            taskExecutor.doAsync(new FactorialTask(mid + 1, end), new FactorialCallback());
            end = mid;
        }

        BigInteger result = BigInteger.ONE;
        if (begin == 0) {
            begin++;
        }
        for (int i = begin; i <= end; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }
}
