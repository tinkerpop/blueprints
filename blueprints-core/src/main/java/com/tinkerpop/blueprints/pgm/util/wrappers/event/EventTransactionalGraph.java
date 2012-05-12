package com.tinkerpop.blueprints.pgm.util.wrappers.event;

import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.WrapperGraph;

public class EventTransactionalGraph<T extends TransactionalGraph> extends EventGraph<T> implements TransactionalGraph, WrapperGraph<T> {

    public EventTransactionalGraph(final T baseGraph) {
        super(baseGraph);
        this.trigger = new EventTrigger(this, true);
    }

    @Override
    public void startTransaction() {
        try {
            this.baseGraph.startTransaction();
            this.trigger.resetEventBuffer();
        } catch (RuntimeException re) {
            throw re;
        }
    }

    @Override
    public void stopTransaction(Conclusion conclusion) {
        try {
            this.baseGraph.stopTransaction(conclusion);
            if (conclusion == Conclusion.SUCCESS) {
                trigger.fireEventBuffer();
            }

            this.trigger.resetEventBuffer();

        } catch (RuntimeException re) {
            throw re;
        }
    }
}
