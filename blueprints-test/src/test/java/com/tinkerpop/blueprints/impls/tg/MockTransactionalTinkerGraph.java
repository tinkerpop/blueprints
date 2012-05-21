package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.TransactionalGraph;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class MockTransactionalTinkerGraph extends TinkerGraph implements TransactionalGraph {
    
    private int numTransactionStarted = 0;
    private int numTransactionsCommitted = 0;
    private int numTransactionsAborted = 0;
    
    @Override
    public void startTransaction() throws IllegalStateException {
        numTransactionStarted++;
    }

    @Override
    public void stopTransaction(Conclusion conclusion) {
        switch(conclusion) {
            case SUCCESS: numTransactionsCommitted++; break;
            case FAILURE: numTransactionsAborted++; break;
            default: throw new IllegalArgumentException("Unrecognized conclusion: " + conclusion);
        }
    }
    
    public int getNumTransactionStarted() {
        return numTransactionStarted;
    }
    
    public int getNumTransactionsCommitted() {
        return numTransactionsCommitted;
    }
    
    public int getNumTransactionsAborted() {
        return numTransactionsAborted;
    }
    
}
