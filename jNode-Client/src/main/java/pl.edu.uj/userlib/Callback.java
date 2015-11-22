package pl.uj.edu.userlib;

public interface Callback {

    void onSuccess(Object taskResult);

    void onFailure(Throwable ex);
}
