package pl.uj.edu.userlib;

public interface Callback {

    void onSuccess(TaskResult taskResult);

    void onFailure(Throwable ex);
}
