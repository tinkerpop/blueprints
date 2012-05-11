package com.tinkerpop.blueprints.pgm;

/**
 * @author Matthias Brocheler (http://matthiasb.com)
 */
public interface ThreadedTransactionalGraph extends TransactionalGraph {

    public TransactionalGraph startThreadTransaction();

}
