package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.TransactionalGraph;

/**
 * Work to be performed within a transaction as part of a TransactionRetryStrategy.
 *
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public interface TransactionWork<T> {

    /**
     * Modify the graph (e.g. add vertices, change properties, etc.).
     *
     * @param graph  The graph to mutate.
     * @return An arbitrary value defined by the implementation of this method.
     * @throws Exception
     */
    public T execute(TransactionalGraph graph) throws Exception;
}
