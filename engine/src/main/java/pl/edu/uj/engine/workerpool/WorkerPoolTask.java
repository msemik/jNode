package pl.edu.uj.engine.workerpool;

import pl.edu.uj.jarpath.Jar;
import pl.edu.uj.userlib.Task;

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
