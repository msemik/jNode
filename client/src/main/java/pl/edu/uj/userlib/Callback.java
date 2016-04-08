package pl.edu.uj.userlib;

import java.io.Serializable;

public interface Callback extends Serializable {
    void onSuccess(Object taskResult);

    void onFailure(Throwable ex);
}
