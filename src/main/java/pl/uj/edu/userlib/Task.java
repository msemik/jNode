package pl.uj.edu.userlib;

import java.io.Serializable;
import java.util.concurrent.Callable;

public interface Task extends Callable<TaskResult>, Serializable {
}
