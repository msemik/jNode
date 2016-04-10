package pl.edu.uj.jnode.engine;

import pl.edu.uj.jnode.jarpath.Jar;
import pl.edu.uj.jnode.userlib.Callback;

public interface CallbackPreExecutionProcessor {
    Callback process(Jar jar, Callback callback);
}
