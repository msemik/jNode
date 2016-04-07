package pl.edu.uj.engine;

import pl.edu.uj.jarpath.Jar;
import pl.uj.edu.userlib.Callback;

public interface CallbackPreExecutionProcessor
{
    Callback process(Jar jar, Callback callback);
}
