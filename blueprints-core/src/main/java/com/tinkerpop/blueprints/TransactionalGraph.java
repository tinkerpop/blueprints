package com.tinkerpop.blueprints;

/**
 * A transactional graph supports the notion of transactions. A transaction scopes a logically coherent operation composed
 * of multiple read and write operations that either occurs at once or not at all. The exact notion of a transaction and its
 * isolational guarantees depend on the implementing graph database.
 *
 * A transaction scopes a coherent and complete operations. Any element references created during a transaction should not
 * be accessed outside its scope (i.e. after the transaction is committed or rolled back). Accessing such references outside
 * the transactional context they were created in may lead to exceptions. If such access is necessary, the transactional
 * context should be extended.
 *
 * By default, the first operation on a TransactionalGraph will start a transaction automatically.
 *
 * @author Matthias Broecheler (http://matthiasb.com)
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface TransactionalGraph extends Graph {

    /**
     * Whether the transaction being stopped was successful (commit) or a failure (rollback).
     */
    @Deprecated
    public enum Conclusion {
        SUCCESS, FAILURE
    }

    /**
     * Stop the current transaction.
     * Specify whether the transaction was successful or not.
     * A failing transaction will rollback all updates to before the transaction was started.
     *
     * If there is currently no open transaction (i.e. no operations on the graph), then calling this method
     * has no effect other than potentially re-initializing data structures.
     *
     * @param conclusion whether or not the current transaction was successful
     */
    @Deprecated
    public void stopTransaction(Conclusion conclusion);

    /**
     * When the graph is shutdown, any open transactions should be successfully committed.
     */
    public void shutdown();

    /**
     * Stop the current transaction and successfully apply mutations to the graph.
     */
    public void commit();

    /**
     * Stop the current transaction and drop any mutations applied since the last transaction.
     */
    public void rollback();

}
