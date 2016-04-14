package pl.edu.uj.jnode.userlib;

import java.io.Serializable;

public interface Callback extends Serializable {
    void onSuccess(Serializable taskResult);

    void onFailure(Throwable ex);
}
