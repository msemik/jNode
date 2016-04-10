package pl.edu.uj.jnode.userlib;

import java.io.Serializable;

public interface Callback extends Serializable {
    void onSuccess(Object taskResult);

    void onFailure(Throwable ex);
}
