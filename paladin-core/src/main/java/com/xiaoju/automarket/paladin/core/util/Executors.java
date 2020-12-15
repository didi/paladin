package com.xiaoju.automarket.paladin.core.util;

import scala.concurrent.ExecutionContext;

public class Executors {

    /**
     * Return a direct execution context. The direct execution context executes the runnable directly
     * in the calling thread.
     *
     * @return Direct execution context.
     */
    public static ExecutionContext directExecutionContext() {
        return DirectExecutionContext.INSTANCE;
    }

    /**
     * Direct execution context.
     */
    private static class DirectExecutionContext implements ExecutionContext {

        static final DirectExecutionContext INSTANCE = new DirectExecutionContext();

        private DirectExecutionContext() {
        }

        @Override
        public void execute(Runnable runnable) {
            runnable.run();
        }

        @Override
        public void reportFailure(Throwable cause) {
            throw new IllegalStateException("Error in direct execution context.", cause);
        }

        @Override
        public ExecutionContext prepare() {
            return this;
        }
    }
}
