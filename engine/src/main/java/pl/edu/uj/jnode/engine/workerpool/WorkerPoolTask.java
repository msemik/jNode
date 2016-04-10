package pl.edu.uj.jnode.engine.workerpool;

import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.userlib.Task;

/**
 * Created by michal on 22.11.15.
 */
public interface WorkerPoolTask extends Task {
    Jar getJar();

    long getTaskId();

    boolean isExternal();

    boolean belongToJar(Jar jar);

    int getPriority();

    void incrementPriority();
}
