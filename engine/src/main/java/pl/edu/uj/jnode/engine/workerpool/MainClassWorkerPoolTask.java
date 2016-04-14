package pl.edu.uj.jnode.engine.workerpool;

import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.userlib.Task;

import java.io.Serializable;

/**
 * Created by michal on 22.11.15.
 */
public class MainClassWorkerPoolTask extends BaseWorkerPoolTask {
    public MainClassWorkerPoolTask(Jar jar, String nodeId) {
        super(jar, nodeId);
    }

    @Override
    public Serializable call() throws Exception {
        return getJar().launchMain();
    }

    @Override
    public Task getRawTask() {
        return this;
    }
}
