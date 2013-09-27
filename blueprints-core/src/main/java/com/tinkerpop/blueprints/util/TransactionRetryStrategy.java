package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.TransactionalGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * The strategy for executing a transaction.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public interface TransactionRetryStrategy<T> {

    /**
     * Executes the TransactionWork with a give strategy.
     *
     * @param graph The TransactionalGraph to mutate.
     * @param work The work to do on the Graph.
     * @return The return value from TransactionWork.
     */
    public T execute(TransactionalGraph graph, TransactionWork<T> work);

    /**
     * Executes the work committing if possible and rolling back on failure.  On failure, an exception is reported.
     */
    public static class OneAndDone<T> implements TransactionRetryStrategy<T> {
        public T execute(final TransactionalGraph graph, final TransactionWork<T> work) {
            T returnValue;
            try {
                returnValue = work.execute(graph);
                graph.commit();
            } catch (Exception e) {
                graph.rollback();
                throw new RuntimeException(e);
            }

            return returnValue;
        }
    }

    /**
     * Executes the work committing if possible and rolling back on failure.  On failure no exception is reported.
     */
    public static class FireAndForget<T> implements TransactionRetryStrategy<T> {
        public T execute(final TransactionalGraph graph, final TransactionWork<T> work) {
            T returnValue = null;
            try {
                returnValue = work.execute(graph);
                graph.commit();
            } catch (Exception e) {
                graph.rollback();
            }

            return returnValue;
        }
    }

    /**
     * Executes the work with a number of retries and with a number of milliseconds delay between each try.
     */
    public static class DelayedRetry<T> extends AbstractRetryStrategy<T> {
        public static final long DEFAULT_DELAY_MS = 20;
        private final long delayBetweenRetry;

        public DelayedRetry() {
            this(DEFAULT_TRIES, DEFAULT_DELAY_MS);
        }

        public DelayedRetry(final int tries, final long delayBetweenRetry) {
            this(tries, delayBetweenRetry, new HashSet<Class>());
        }

        public DelayedRetry(final int tries, final long delayBetweenRetry, final Set<Class> exceptionsToRetryOn) {
            super(tries, exceptionsToRetryOn);
            this.delayBetweenRetry = delayBetweenRetry;
        }

        @Override
        long getDelay(final int retryCount) {
            return this.delayBetweenRetry;
        }
    }

    /**
     * Executes the work with a number of retries and with a exponentially increasing number of milliseconds
     * between each retry.
     */
    public static class ExponentialBackoff<T> extends AbstractRetryStrategy<T> {
        public static final long DEFAULT_DELAY_MS = 20;
        private final long initialDelay;

        public ExponentialBackoff() {
            this(DEFAULT_TRIES, DEFAULT_DELAY_MS);
        }

        public ExponentialBackoff(final int tries, final long initialDelay) {
            this(tries, initialDelay, new HashSet<Class>());
        }

        public ExponentialBackoff(final int tries, final long initialDelay, final Set<Class> exceptionsToRetryOn) {
            super(tries, exceptionsToRetryOn);
            this.initialDelay = initialDelay;
        }

        @Override
        long getDelay(final int retryCount) {
            return (long) (initialDelay * Math.pow(2, retryCount));
        }
    }

    /**
     * Base class for strategy that require a retry.
     */
    public static abstract class AbstractRetryStrategy<T> implements TransactionRetryStrategy<T> {
        public static final int DEFAULT_TRIES = 8;

        protected final int tries;
        protected final Set<Class> exceptionsToRetryOn;

        public AbstractRetryStrategy() {
            this(DEFAULT_TRIES, new HashSet<Class>());
        }

        public AbstractRetryStrategy(final int tries, final Set<Class> exceptionsToRetryOn) {
            this.tries = tries;
            this.exceptionsToRetryOn = exceptionsToRetryOn;
        }

        abstract long getDelay(int retryCount);

        public T execute(final TransactionalGraph graph, final TransactionWork<T> work) {
            T returnValue;

            // this is the default exception...it may get reassgined during retries
            Exception previousException = new RuntimeException("Exception initialized when trying commit.");

            // try to commit a few times
            for (int ix = 0; ix < tries; ix++) {

                // increase time after each failed attempt though there is no delay on the first try
                if (ix > 0)
                    try { Thread.sleep(getDelay(ix)); } catch (InterruptedException ie) { }

                try {
                    returnValue = work.execute(graph);
                    graph.commit();

                    // need to exit the function here so that retries don't happen
                    return returnValue;
                } catch (Exception ex) {
                    graph.rollback();

                    // retry if this is an allowed exception otherwise, just throw and go
                    boolean retry = false;
                    if (this.exceptionsToRetryOn.size() == 0)
                        retry = true;
                    else {
                        for (Class exceptionToRetryOn : this.exceptionsToRetryOn) {
                            if (ex.getClass().equals(exceptionToRetryOn)) {
                                retry = true;
                                break;
                            }
                        }
                    }

                    if (!retry) {
                        throw new RuntimeException(ex);
                    }

                    previousException = ex;
                }
            }

            // the exception just won't go away after all the retries
            throw new RuntimeException(previousException);
        }
    }
}
