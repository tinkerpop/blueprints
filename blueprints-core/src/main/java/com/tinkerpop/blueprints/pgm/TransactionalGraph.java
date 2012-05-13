package com.tinkerpop.blueprints.pgm;

/**
 * A transactional graph supports the notion of transactions.
 * Once a transaction is started, all write operations can either be committed or rolled back.
 * Read operations are not required to be in a transaction.
 * A transactional graph supports automatic transaction handling with user-defined transaction buffer size.
 * All constructed transactional graphs begin in with a transaction buffer size of 1 (thus, every mutation is a commit).
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
     * This is required for graph manipulations in manual transaction mode.
     *
     * @throws RuntimeException If a transaction is already in progress, then a RuntimeException of "nested transaction" is thrown.
     */
    public void startTransaction();

    /**
     * Stop the current transaction. If the current buffer still has active mutations, then they are committed.
     * Specify whether the transaction was successful or not.
     * A failing transaction will rollback all updates to before the transaction was started.
     *
     * @param conclusion whether or not the current transaction was successful or not
     */
    public void stopTransaction(Conclusion conclusion);

    /**
     * When the graph is shutdown, any open transaction is committed successfully.
     */
    public void shutdown();

}
