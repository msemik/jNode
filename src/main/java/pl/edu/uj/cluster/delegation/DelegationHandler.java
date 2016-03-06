package pl.edu.uj.cluster.delegation;

public interface DelegationHandler {
    void handleDuringOnWorkerPoolEvent();

    void handleDuringOnHeartBeat();
}
