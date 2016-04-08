package pl.edu.uj.userlib;

public interface Callback {
    void onSuccess(Object taskResult);

    void onFailure(Throwable ex);
}
