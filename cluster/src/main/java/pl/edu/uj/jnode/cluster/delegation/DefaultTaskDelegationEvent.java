package pl.edu.uj.jnode.cluster.delegation;

import java.util.Optional;

enum DefaultTaskDelegationEvent implements DelegationEvent {
    HEARTBEAT {
        @Override
        public State nextState(DelegationHandler handler, State state) {
            return state.nextStateOnHeartBeat(handler);
        }

        @Override
        public Optional<DelegationEvent> executeAction(FSMBasedDelegationHandler handler, State state) {
            return state.onHeartBeat(handler);
        }
    }, OVERFLOW {
        @Override
        public State nextState(DelegationHandler handler, State state) {
            return state.nextStateOnWorkerPoolOverflow(handler);
        }

        @Override
        public Optional<DelegationEvent> executeAction(FSMBasedDelegationHandler handler, State state) {
            return state.onWorkerPoolOverflow(handler);
        }
    }, NO_THREADS {
        @Override
        public State nextState(DelegationHandler handler, State state) {
            return state.nextStateOnNoThreads(handler);
        }

        @Override
        public Optional<DelegationEvent> executeAction(FSMBasedDelegationHandler handler, State state) {
            return state.onNoThreads(handler);
        }
    }, TASK_DELEGATION_FINISHED {
        @Override
        public State nextState(DelegationHandler handler, State state) {
            return state.nextStateOnTaskDelegationFinished(handler);
        }

        @Override
        public Optional<DelegationEvent> executeAction(FSMBasedDelegationHandler handler, State state) {
            return state.onTaskDelegationFinished(handler);
        }
    }
}
