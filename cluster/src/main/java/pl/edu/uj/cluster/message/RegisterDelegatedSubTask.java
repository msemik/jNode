package pl.edu.uj.cluster.message;

import pl.edu.uj.cluster.Distributable;
import pl.edu.uj.cluster.Distributor;
import pl.edu.uj.cluster.callback.SerializableCallback;
import pl.edu.uj.cluster.task.ExternalTask;
import pl.edu.uj.userlib.Callback;

import java.io.Serializable;
import java.util.Optional;

/**
 * Created by alanhawrot on 08.04.2016.
 */
public class RegisterDelegatedSubTask implements Serializable, Distributable {
    private ExternalTask externalTask;
    private SerializableCallback callback;

    public RegisterDelegatedSubTask(ExternalTask externalTask, SerializableCallback callback) {
        this.externalTask = externalTask;
        this.callback = callback;
    }

    public ExternalTask getExternalTask() {
        return externalTask;
    }

    public SerializableCallback getCallback() {
        return callback;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onRegisterDelegatedSubTask(sourceNodeId, externalTask, callback);
    }

    @Override
    public String toString() {
        return "RegisterDelegatedSubTask{" +
               "externalTask=" + externalTask +
               ", callback=" + callback +
               '}';
    }
}
