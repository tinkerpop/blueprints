package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.TransactionalGraph;

/**
 * Helps handle the retry of failed transactions utilizing different pluggable retry strategies.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class TransactionGraphHelper {

    /**
     * Create a new transaction helper class for TransactionalGraph.
     * @param graph The graph to mutate.
     * @param <T> The return type for the TransactionWork
     * @return A RetryHelper object which allows the retry behavior.
     */
    public static <T> RetryHelper<T> newRetryHelper(final TransactionalGraph graph) {
        return new RetryHelper<T>(graph);
    }

    /**
     * Creates a TransactionGraph "holder" which allows execution of a TransactionWork instance inside of a
     * TransactionStrategy implementation.
     *
     * @param <T> The return value of the work.
     */
    public static class RetryHelper<T> {
        private final TransactionalGraph graph;

        private RetryHelper(final TransactionalGraph graph) {
            this.graph = graph;
        }

        /**
         * Executes the work committing if possible and rolling back on failure.  On failure, not exception is reported.
         */
        public T fireAndForget(final TransactionWork<T> work) {
            return tryIt(work, new TransactionStrategy.FireAndForget<T>());
        }

        /**
         * Executes the work committing if possible and rolling back on failure.  On failure an exception is reported.
         */
        public T oneAndDone(final TransactionWork<T> work) {
            return tryIt(work, new TransactionStrategy.OneAndDone<T>());
        }

        /**
         * Executes the work with a default number of retries with a default number of milliseconds delay between
         * each try.
         */
        public T retry(final TransactionWork<T> work) {
            return tryIt(work, new TransactionStrategy.DelayedRetry<T>());
        }

        /**
         * Executes the work with a specified number of retries with a default number of milliseconds delay between
         * each try.
         */
        public T retry(final TransactionWork<T> work, final int retries) {
            return tryIt(work, new TransactionStrategy.DelayedRetry<T>(retries, TransactionStrategy.DelayedRetry.DEFAULT_DELAY_MS));
        }

        /**
         * Executes the work with a specified number of retries with a specified number of milliseconds delay between
         * each try.
         */
        public T retry(final TransactionWork<T> work, final int retries, final long delayBetweenRetries) {
            return tryIt(work, new TransactionStrategy.DelayedRetry<T>(retries, delayBetweenRetries));
        }

        /**
         * Executes the work with a default number of retries with a exponentially increasing number of milliseconds
         * between each retry.
         */
        public T exponentialBackoff(final TransactionWork<T> work) {
            return tryIt(work, new TransactionStrategy.ExponentialBackoff<T>());
        }

        /**
         * Executes the work with a specified number of retries with a exponentially increasing number of milliseconds
         * between each retry.
         */
        public T exponentialBackoff(final TransactionWork<T> work, final int retries) {
            return tryIt(work, new TransactionStrategy.ExponentialBackoff<T>(
                    retries, TransactionStrategy.ExponentialBackoff.DEFAULT_TRIES));
        }

        /**
         * Executes the work with a specified TransactionStrategy.
         */
        public T tryIt(final TransactionWork<T> work, final TransactionStrategy<T> strategy) {
            return strategy.execute(this.graph, work);
        }
    }
}
