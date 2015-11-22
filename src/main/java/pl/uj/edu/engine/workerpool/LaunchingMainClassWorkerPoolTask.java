package pl.uj.edu.engine.workerpool;

import pl.uj.edu.engine.eventloop.EventLoopThread;

/**
 * Created by michal on 22.11.15.
 */
public class LaunchingMainClassWorkerPoolTask extends BaseWorkerPoolTask {
    private EventLoopThread eventLoopThread;

    public LaunchingMainClassWorkerPoolTask(EventLoopThread eventLoopThread) {
        super(eventLoopThread.getJarName());
        this.eventLoopThread = eventLoopThread;
    }

    @Override
    public Object call() throws Exception {
        return eventLoopThread.getJarLauncher().launchMain();
    }
}
