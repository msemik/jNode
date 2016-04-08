package pl.edu.uj.engine.workerpool;

import pl.edu.uj.jarpath.Jar;

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
