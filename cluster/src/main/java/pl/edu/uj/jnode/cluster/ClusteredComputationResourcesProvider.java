package pl.edu.uj.jnode.cluster;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import pl.edu.uj.jnode.cluster.node.Nodes;
import pl.edu.uj.jnode.engine.*;

@Component
@Primary
public class ClusteredComputationResourcesProvider implements ComputationResourcesProvider
{
    @Autowired
    private Nodes nodes;
    @Autowired
    private WorkerPoolComputationResourcesProvider workerPoolComputationResourcesProvider;

    @Override public long getAvailableWorkers()
    {
        return nodes.getAvailableWorkers() + workerPoolComputationResourcesProvider.getAvailableWorkers();
    }

    @Override public long getTotalWorkers()
    {
        return nodes.getTotalWorkers() + workerPoolComputationResourcesProvider.getTotalWorkers();
    }
}
