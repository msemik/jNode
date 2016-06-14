package pl.edu.uj.jnode.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.engine.workerpool.WorkerPool;

@Component
public class WorkerPoolComputationResourcesProvider implements ComputationResourcesProvider
{
    @Autowired
    private WorkerPool workerPool;

    @Override public long getAvailableWorkers()
    {
        return workerPool.poolSize() - workerPool.jobsInPool();
    }

    @Override public long getTotalWorkers()
    {
        return workerPool.poolSize();
    }
}
