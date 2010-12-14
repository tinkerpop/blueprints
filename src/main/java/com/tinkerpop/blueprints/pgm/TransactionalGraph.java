package com.tinkerpop.blueprints.pgm;

/**
 * A transactional graph supports the notion of transactions.
 * Once a transaction is started, all write operations can either be committed or rolled back.
 * Read operations are not required to be in a transaction.
 * A transactional graph can be in two modes: automatic or manual.
 * All constructed transactional graphs begin in automatic transaction mode.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface TransactionalGraph extends Graph {

    /**
     * Error message to use when code is trying to stop/stop a transaction in automatic transaction mode.
     */
    public static final String TURN_OFF_MESSAGE = "Turn off automatic transactions to use manual transaction handling";
    public static final String NESTED_MESSAGE = "Stop current transaction before starting another";

    /**
     * Whether the transaction being stopped was successful (commit) or a failure (rollback).
     */
    public enum Conclusion {
        SUCCESS, FAILURE
    }

    /**
     * Transactions in a transactional graph can either be handled automatically or manually.
     * If the graph is in automatic mode, then every mutation to the graph will committed at the time of mutation.
     * A mutation is atomic up to the write methods of graph/element/index.
     * If the graph is in manual model, then the user is responsible for starting and stopping transactions.
     */
    public enum Mode {
        AUTOMATIC, MANUAL
    }

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

    /**
     * There are two transaction modes: automatic and manual.
     * Automatic transactions commit successfully on every operation.
     * The benefit of auto transactions is that it requires less code.
     * The drawback of auto transactions is that it is much slower than manually controlled transactions as every write/delete operation is committed when complete.
     *
     * @param mode the transaction mode to use
     */
    public void setTransactionMode(Mode mode);

    /**
     * Returns the transaction mode the graph is currently in.
     *
     * @return returns the graph's transaction mode
     */
    public Mode getTransactionMode();
}
