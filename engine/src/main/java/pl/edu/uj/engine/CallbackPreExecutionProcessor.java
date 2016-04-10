package pl.edu.uj.engine;

import pl.edu.uj.jarpath.Jar;
import pl.edu.uj.userlib.Callback;

public interface CallbackPreExecutionProcessor {
    Callback process(Jar jar, Callback callback);
}
