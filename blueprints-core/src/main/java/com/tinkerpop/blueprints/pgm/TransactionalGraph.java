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
     * Error message to use when code is trying to start another transaction before stopping the previous.
     */
    public static final String NESTED_MESSAGE = "Stop current transaction before starting another";

    /**
     * Whether the transaction being stopped was successful (commit) or a failure (rollback).
     */
    public enum Conclusion {
        SUCCESS, FAILURE
    }

    /**
     * Transactions in a transactional graph can either be handled automatically when provided a bufferSize > 0.
     * If the graph is automatically handling transactions, then every X mutations to the graph will committed, where X is the bufferSize.
     * A mutation is atomic up to the write methods of graph/element/index.
     * If the graph has a bufferSize of 0, then the user is responsible for starting and stopping transactions.
     *
     * @param bufferSize 0 for manual transactions and > 0 for automatic transaction handling
     */
    public void setTransactionBuffer(int bufferSize);

    /**
     * Returns the size of the transaction buffer.
     *
     * @return the transaction buffer size
     */
    public int getTransactionBuffer();

    /**
     * Start a transaction in order to manipulate the graph.
     * This is required for graph manipulations in manual transaction mode.
     */
    public void startTransaction();

    /**
     * Stop the current transaction. This is possible only in manual transaction mode.
     * Specify whether the transaction was successful or not.
     * A failing transaction will rollback all updates to before the transaction was started.
     *
     * @param conclusion whether or not the current transaction was successful or not
     */
    public void stopTransaction(Conclusion conclusion);
}
