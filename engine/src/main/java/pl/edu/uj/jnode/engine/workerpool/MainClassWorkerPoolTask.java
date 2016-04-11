package pl.edu.uj.jnode.engine.workerpool;

import pl.edu.uj.jnode.jarpath.Jar;

/**
 * Created by michal on 22.11.15.
 */
public class MainClassWorkerPoolTask extends BaseWorkerPoolTask {
    public MainClassWorkerPoolTask(Jar jar) {
        super(jar);
    }

    @Override
    public Object call() throws Exception {
        return getJar().launchMain();
    }
}