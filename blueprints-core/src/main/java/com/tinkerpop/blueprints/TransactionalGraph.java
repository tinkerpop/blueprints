package com.tinkerpop.blueprints;

/**
 * A transactional graph supports the notion of transactions.
 * Once a transaction is started, all write operations can either be committed or rolled back.
 * If an operation is transactional and there currently exists no transaction, then a transaction must be automatically started.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface TransactionalGraph extends Graph {

    /**
     * Whether the transaction being stopped was successful (commit) or a failure (rollback).
     */
    public enum Conclusion {
        SUCCESS, FAILURE
    }

    /**
     * Start a transaction in order to manipulate the graph.
     *
     * @throws IllegalStateException If a transaction is already in progress, then a "nested transaction exception" is thrown.
     */
    public void startTransaction() throws IllegalStateException;

    /**
     * Stop the current transaction.
     * Specify whether the transaction was successful or not.
     * A failing transaction will rollback all updates to before the transaction was started.
     *
     * @param conclusion whether or not the current transaction was successful or not
     */
    public void stopTransaction(Conclusion conclusion);

    /**
     * When the graph is shutdown, any open transactions should be successfully committed.
     */
    public void shutdown();

}
