package com.tinkerpop.blueprints.pgm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface TransactionalGraph extends Graph {

    public static final String TURN_OFF_MESSAGE = "Turn off automatic transactions to use manual transaction handling";

    /**
     * Start a transaction to perform graph manipulation operations within.
     */
    public void startTransaction();

    /**
     * Stop the current transaction. Specify whether the transaction was successful or not.
     * An unsuccessful transaction will rollback all updates to when the transaction was started.
     *
     * @param success whether or not the current transaction was successful or not
     */
    public void stopTransaction(boolean success);

    /**
     * Automatic transactions commit successfully on every operation.
     * The benefit of auto transactions is that it requires less method calls in the users code.
     * The drawback of auto transactions is that it is much slower than user controlled transactions
     * as every write/delete operation is committed when complete.
     *
     * @param autoTransactions whether or not to use automatic transactions
     */
    public void setAutoTransactions(boolean autoTransactions);

    /**
     * Returns whether automatic transactions are turned on or not.
     *
     * @return returns true if automatic transactions are turned on, else it returns false.
     */
    public boolean isAutoTransactions();
}
