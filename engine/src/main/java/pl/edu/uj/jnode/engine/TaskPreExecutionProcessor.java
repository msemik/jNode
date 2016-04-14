package pl.edu.uj.jnode.engine;

import pl.edu.uj.jnode.engine.workerpool.WorkerPoolTask;

public interface TaskPreExecutionProcessor {
    void process(WorkerPoolTask task);
}
