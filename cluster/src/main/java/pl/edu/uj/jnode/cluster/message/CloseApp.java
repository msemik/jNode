package pl.edu.uj.jnode.cluster.message;

import pl.edu.uj.jnode.cluster.Distributable;
import pl.edu.uj.jnode.cluster.Distributor;
import pl.edu.uj.jnode.cluster.task.ExternalTask;

import java.io.Serializable;
import java.util.Optional;

/**
 * Created by alanhawrot on 17.06.2016.
 */
public class CloseApp implements Serializable, Distributable {
    private ExternalTask externalTask;

    public CloseApp(ExternalTask externalTask) {
        this.externalTask = externalTask;
    }

    @Override
    public void distribute(Distributor distributor, String sourceNodeId, Optional<String> destinationNodeId) {
        distributor.onCloseApp(sourceNodeId, externalTask);
    }

    @Override
    public String toString() {
        return "CloseApp{" +
               "externalTask=" + externalTask +
               '}';
    }
}
