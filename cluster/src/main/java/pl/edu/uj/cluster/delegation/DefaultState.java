package pl.edu.uj.cluster.delegation;

import java.util.Optional;

import static java.util.Optional.empty;

enum DefaultState implements State {
    NO_DELEGATION {
        @Override
        public Optional<DelegationEvent> onHeartBeat(FSMBasedDelegationHandler handler) {
            return handler.empty();
        }

        @Override
        public State nextStateOnHeartBeat(DelegationHandler handler) {
            return NO_DELEGATION;
        }

        @Override
        public Optional<DelegationEvent> onWorkerPoolOverflow(FSMBasedDelegationHandler handler) {
            return handler.delegateTasks();
        }

        @Override
        public State nextStateOnWorkerPoolOverflow(DelegationHandler handler) {
            return DURING_DELEGATION;
        }

        @Override
        public Optional<DelegationEvent> onTaskDelegationFinished(FSMBasedDelegationHandler handler) {
            return handler.error();
        }

        @Override
        public State nextStateOnTaskDelegationFinished(DelegationHandler handler) {
            return NO_DELEGATION;
        }

        @Override
        public Optional<DelegationEvent> onNoThreads(FSMBasedDelegationHandler handler) {
            return handler.error();
        }

        @Override
        public State nextStateOnNoThreads(DelegationHandler handler) {
            return NO_DELEGATION;
        }
    },
    DURING_DELEGATION {
        @Override
        public Optional<DelegationEvent> onHeartBeat(FSMBasedDelegationHandler handler) {
            return handler.empty();
        }

        @Override
        public State nextStateOnHeartBeat(DelegationHandler handler) {
            return SCHEDULED_RE_EXECUTION;
        }

        @Override
        public Optional<DelegationEvent> onWorkerPoolOverflow(FSMBasedDelegationHandler handler) {
            return handler.empty();
        }

        @Override
        public State nextStateOnWorkerPoolOverflow(DelegationHandler handler) {
            return SCHEDULED_RE_EXECUTION;
        }

        @Override
        public Optional<DelegationEvent> onTaskDelegationFinished(FSMBasedDelegationHandler handler) {
            return handler.empty();
        }

        @Override
        public State nextStateOnTaskDelegationFinished(DelegationHandler handler) {
            return NO_DELEGATION;
        }

        @Override
        public Optional<DelegationEvent> onNoThreads(FSMBasedDelegationHandler handler) {
            return handler.empty();
        }

        @Override
        public State nextStateOnNoThreads(DelegationHandler handler) {
            return AWAITING_THREADS;
        }
    },
    SCHEDULED_RE_EXECUTION {
        @Override
        public Optional<DelegationEvent> onHeartBeat(FSMBasedDelegationHandler handler) {
            return handler.empty();
        }

        @Override
        public State nextStateOnHeartBeat(DelegationHandler handler) {
            return SCHEDULED_RE_EXECUTION;
        }

        @Override
        public Optional<DelegationEvent> onWorkerPoolOverflow(FSMBasedDelegationHandler handler) {
            return handler.empty();
        }

        @Override
        public State nextStateOnWorkerPoolOverflow(DelegationHandler handler) {
            return SCHEDULED_RE_EXECUTION;
        }

        @Override
        public Optional<DelegationEvent> onTaskDelegationFinished(FSMBasedDelegationHandler handler) {
            return handler.delegateTasks();
        }

        @Override
        public State nextStateOnTaskDelegationFinished(DelegationHandler handler) {
            return DURING_DELEGATION;
        }

        @Override
        public Optional<DelegationEvent> onNoThreads(FSMBasedDelegationHandler handler) {
            return empty();
        }

        @Override
        public State nextStateOnNoThreads(DelegationHandler handler) {
            return AWAITING_THREADS;
        }
    },
    AWAITING_THREADS {
        @Override
        public Optional<DelegationEvent> onHeartBeat(FSMBasedDelegationHandler handler) {
            return handler.delegateTasks();
        }

        @Override
        public State nextStateOnHeartBeat(DelegationHandler handler) {
            return DURING_DELEGATION;
        }

        @Override
        public Optional<DelegationEvent> onWorkerPoolOverflow(FSMBasedDelegationHandler handler) {
            return handler.empty();
        }

        @Override
        public State nextStateOnWorkerPoolOverflow(DelegationHandler handler) {
            return AWAITING_THREADS;
        }

        @Override
        public Optional<DelegationEvent> onTaskDelegationFinished(FSMBasedDelegationHandler handler) {
            return handler.error();
        }

        @Override
        public State nextStateOnTaskDelegationFinished(DelegationHandler handler) {
            return AWAITING_THREADS;
        }

        @Override
        public Optional<DelegationEvent> onNoThreads(FSMBasedDelegationHandler handler) {
            return handler.error();
        }

        @Override
        public State nextStateOnNoThreads(DelegationHandler handler) {
            return AWAITING_THREADS;
        }
    };
}
