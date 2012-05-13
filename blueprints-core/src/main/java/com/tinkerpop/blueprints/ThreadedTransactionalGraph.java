package com.tinkerpop.blueprints;

/**
 * @author Matthias Brocheler (http://matthiasb.com)
 */
public interface ThreadedTransactionalGraph extends TransactionalGraph {

    public TransactionalGraph startThreadTransaction();

}
