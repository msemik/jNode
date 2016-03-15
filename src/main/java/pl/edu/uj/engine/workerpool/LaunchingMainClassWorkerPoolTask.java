package pl.edu.uj.engine.workerpool;

import pl.edu.uj.jarpath.Jar;

/**
 * Created by michal on 22.11.15.
 */
public class LaunchingMainClassWorkerPoolTask extends BaseWorkerPoolTask {
    private transient Jar jar;

    public LaunchingMainClassWorkerPoolTask(Jar jar) {
        super(jar);
        this.jar = jar;
    }

    @Override
    public Object call() throws Exception {
        return getJar().launchMain();
    }

    @Override
    public Jar getJar() {
        return jar;
    }

    public void setJar(Jar jar) {
        this.jar = jar;
    }
}
