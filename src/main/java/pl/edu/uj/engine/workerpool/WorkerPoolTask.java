package pl.edu.uj.engine.workerpool;

import pl.uj.edu.userlib.Task;

import java.nio.file.Path;

/**
 * Created by michal on 22.11.15.
 */
public interface WorkerPoolTask extends Task {

    Path getJarName();

    long getTaskId();

    boolean isExternal();

    boolean belongToJar(Path jarFileName);
}
