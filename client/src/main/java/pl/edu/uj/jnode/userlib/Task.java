package pl.edu.uj.jnode.userlib;

import java.io.Serializable;
import java.util.concurrent.Callable;

public interface Task extends Callable<Serializable>, Serializable {
}
