package pl.edu.uj.jnode.cluster.delegation;

public interface DelegationHandler {
    void handleDuringOnWorkerPoolEvent();

    void handleDuringOnHeartBeat();
}
