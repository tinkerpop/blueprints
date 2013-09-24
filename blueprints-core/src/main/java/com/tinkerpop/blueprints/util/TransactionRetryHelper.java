package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.TransactionalGraph;

import java.util.Set;

/**
 * Creates a TransactionGraph "holder" which allows execution of a TransactionWork instance inside of a
 * TransactionRetryStrategy implementation.
 *
 * @param <T> The return value of the work.
 */
public class TransactionRetryHelper<T> {
    private final TransactionalGraph graph;
    private final TransactionWork<T> work;

    private TransactionRetryHelper(final Builder<T> builder) {
        this.graph = builder.getGraph();
        this.work = builder.getWork();
    }

    /**
     * Executes the work committing if possible and rolling back on failure.  On failure, not exception is reported.
     */
    public T fireAndForget() {
        return use(new TransactionRetryStrategy.FireAndForget<T>());
    }

    /**
     * Executes the work committing if possible and rolling back on failure.  On failure an exception is reported.
     */
    public T oneAndDone() {
        return use(new TransactionRetryStrategy.OneAndDone<T>());
    }

    /**
     * Executes the work with a default number of retries with a default number of milliseconds delay between
     * each try.
     */
    public T retry() {
        return use(new TransactionRetryStrategy.DelayedRetry<T>());
    }

    /**
     * Executes the work with a specified number of retries with a default number of milliseconds delay between
     * each try.
     */
    public T retry(final int retries) {
        return use(new TransactionRetryStrategy.DelayedRetry<T>(retries, TransactionRetryStrategy.DelayedRetry.DEFAULT_DELAY_MS));
    }

    /**
     * Executes the work with a specified number of retries with a specified number of milliseconds delay between
     * each try.
     */
    public T retry(final int retries, final long delayBetweenRetries) {
        return use(new TransactionRetryStrategy.DelayedRetry<T>(retries, delayBetweenRetries));
    }

    /**
     * Executes the work with a specified number of retries with a specified number of milliseconds delay between
     * each try.
     *
     * @param exceptionsToRetryOn For a retry to happen, it must be in this set of accepted exceptions.  If an
     *                            exception raises that is not in the set, then an error is immediately raised
     *                            with no retry.
     */
    public T retry(final int retries, final long delayBetweenRetries, final Set<Class> exceptionsToRetryOn ) {
        return use(new TransactionRetryStrategy.DelayedRetry<T>(retries, delayBetweenRetries, exceptionsToRetryOn));
    }

    /**
     * Executes the work with a default number of retries with a exponentially increasing number of milliseconds
     * between each retry.
     */
    public T exponentialBackoff() {
        return use(new TransactionRetryStrategy.ExponentialBackoff<T>());
    }

    /**
     * Executes the work with a specified number of retries with a exponentially increasing number of milliseconds
     * between each retry.
     */
    public T exponentialBackoff(final int retries) {
        return use(new TransactionRetryStrategy.ExponentialBackoff<T>(
                retries, TransactionRetryStrategy.ExponentialBackoff.DEFAULT_DELAY_MS));
    }

    /**
     * Executes the work with a specified number of retries with a exponentially increasing number of milliseconds
     * between each retry.
     */
    public T exponentialBackoff(final int retries, final long initialDelay) {
        return use(new TransactionRetryStrategy.ExponentialBackoff<T>(
                retries, initialDelay));
    }

    /**
     * Executes the work with a specified number of retries with a exponentially increasing number of milliseconds
     * between each retry.
     *
     * @param exceptionsToRetryOn For a retry to happen, it must be in this set of accepted exceptions.  If an
     *                            exception raises that is not in the set, then an error is immediately raised
     *                            with no retry.
     */
    public T exponentialBackoff(final int retries, final long initialDelay, final Set<Class> exceptionsToRetryOn) {
        return use(new TransactionRetryStrategy.ExponentialBackoff<T>(
                retries, initialDelay, exceptionsToRetryOn));
    }

    /**
     * Executes the work with a specified TransactionRetryStrategy.
     */
    public T use(final TransactionRetryStrategy<T> strategy) {
        return strategy.execute(this.graph, this.work);
    }

    /**
     * Constructs a TransactionRetryStrategy.
     */
    public static class Builder<T> {
        private final TransactionalGraph graph;
        private TransactionWork<T> work;

        public Builder(final TransactionalGraph graph) {
            this.graph = graph;
        }

        public Builder<T> perform(final TransactionWork<T> work) {
            this.work = work;
            return this;
        }

        public TransactionRetryHelper<T> build() {
            return new TransactionRetryHelper<T>(this);
        }

        public TransactionalGraph getGraph() {
            return graph;
        }

        public TransactionWork<T> getWork() {
            return work;
        }
    }
}
