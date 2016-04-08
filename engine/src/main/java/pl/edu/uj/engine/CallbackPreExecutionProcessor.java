package pl.edu.uj.engine;

import pl.edu.uj.userlib.Callback;

public interface CallbackPreExecutionProcessor
{
    Callback process(Callback callback);
}
