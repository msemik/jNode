package pl.edu.uj.engine;

import pl.uj.edu.userlib.Callback;

public interface CallbackPreExecutionProcessor
{
    Callback process(Callback callback);
}
