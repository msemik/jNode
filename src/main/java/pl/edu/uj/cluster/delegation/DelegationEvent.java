package pl.edu.uj.cluster.delegation;

import java.util.Optional;

interface DelegationEvent {
    State nextState(DelegationHandler handler, State state);

    /**
     * Executes action bound with pair event and state.
     * @param state state before transition
     * @return next operation to execute (if any)
     */
    Optional<DelegationEvent> executeAction(FSMBasedDelegationHandler handler, State state);
}
