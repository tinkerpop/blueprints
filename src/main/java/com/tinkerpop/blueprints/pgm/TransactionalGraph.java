package com.tinkerpop.blueprints.pgm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface TransactionalGraph extends Graph {

    public static final String TURN_OFF_MESSAGE = "Turn off automatic transactions to use manual transaction handling";

    public enum Conclusion {
        SUCCESS, FAILURE
    }

    public enum Mode {
        MANUAL, AUTOMATIC
    }

    /**
     * Start a transaction to perform graph manipulation operations within.
     */
    public void startTransaction();

    /**
     * Stop the current transaction. Specify whether the transaction was successful or not.
     * An failing transaction will rollback all updates to when the transaction was started.
     *
     * @param conclusion whether or not the current transaction was successful or not
     */
    public void stopTransaction(Conclusion conclusion);

    /**
     * There are two transaction modes: automatic and manual.
     * Automatic transactions commit successfully on every operation.
     * The benefit of auto transactions is that it requires less method calls in the users code.
     * The drawback of auto transactions is that it is much slower than manually controlled transactions
     * as every write/delete operation is committed when complete.
     *
     * @param mode the transaction mode to use
     */
    public void setTransactionMode(Mode mode);

    /**
     * Returns the transaction mode the graph is currently in.
     *
     * @return returns the graphs transaction mode
     */
    public Mode getTransactionMode();
}
