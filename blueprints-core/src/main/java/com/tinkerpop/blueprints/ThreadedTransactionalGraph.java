package com.tinkerpop.blueprints;

/**
 * ThreadedTransactionalGraph provides more fine grained control over the transactional context.
 * While TransactionalGraph binds each transaction to the executing thread, ThreadedTransactionalGraph's {@link #newTransaction} returns a {@link TransactionalGraph} that represents its own transactional context independent of the executing thread.
 * Hence, one can have multiple threads operating against a single transaction represented by the returned TransactionalGraph object. This is useful for parallelizing graph algorithms.
 *
 * Note, that one needs to call {@link TransactionalGraph#commit()} or {@link TransactionalGraph#rollback()} to close the transactions returned
 * by {@link #newTransaction()}.
 *
 * @author Matthias Brocheler (http://matthiasb.com)
 */
public interface ThreadedTransactionalGraph extends TransactionalGraph {

    /**
     * Returns a {@link TransactionalGraph} that represents a transactional context independent of the executing transaction.
     *
     * @return A transactional context. Invoking TransactionalGraph.shutdown() successfully commits the transaction.
     */
    public TransactionalGraph newTransaction();

}
