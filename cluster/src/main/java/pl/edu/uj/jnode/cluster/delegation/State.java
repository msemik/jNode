package pl.edu.uj.jnode.cluster.delegation;

import java.util.Optional;

interface State {
    Optional<DelegationEvent> onHeartBeat(FSMBasedDelegationHandler handler);

    State nextStateOnHeartBeat(DelegationHandler handler);

    Optional<DelegationEvent> onWorkerPoolOverflow(FSMBasedDelegationHandler handler);

    State nextStateOnWorkerPoolOverflow(DelegationHandler handler);

    Optional<DelegationEvent> onTaskDelegationFinished(FSMBasedDelegationHandler handler);

    State nextStateOnTaskDelegationFinished(DelegationHandler handler);

    Optional<DelegationEvent> onNoThreads(FSMBasedDelegationHandler handler);

    State nextStateOnNoThreads(DelegationHandler handler);
}
