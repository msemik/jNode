package pl.edu.uj.jnode.cluster.message;

import pl.edu.uj.jnode.cluster.Distributable;
import pl.edu.uj.jnode.cluster.Distributor;
import pl.edu.uj.jnode.cluster.callback.SerializableCallbackWrapper;
import pl.edu.uj.jnode.cluster.task.ExternalTask;

import java.io.Serializable;
import java.util.Optional;

/**
 * Created by alanhawrot on 08.04.2016.
 */
public class RegisterDelegatedSubTask implements Serializable, Distributable {
    private ExternalTask externalTask;
    private SerializableCallbackWrapper callback;

    public RegisterDelegatedSubTask(ExternalTask externalTask, SerializableCallbackWrapper callback) {
        this.externalTask = externalTask;
        this.callback = callback;
    }

    public ExternalTask getExternalTask() {
        return externalTask;
    }

    public SerializableCallbackWrapper getCallback() {
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
