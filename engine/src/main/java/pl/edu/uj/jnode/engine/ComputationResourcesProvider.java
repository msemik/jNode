package pl.edu.uj.jnode.engine;

public interface ComputationResourcesProvider
{
    long getAvailableWorkers();
    long getTotalWorkers();
}
