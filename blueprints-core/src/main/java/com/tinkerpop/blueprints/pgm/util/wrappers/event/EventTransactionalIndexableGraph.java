package com.tinkerpop.blueprints.pgm.util.wrappers.event;

import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.WrapperGraph;

public class EventTransactionalIndexableGraph <T extends IndexableGraph & TransactionalGraph> extends EventIndexableGraph<T>
        implements TransactionalGraph, IndexableGraph, WrapperGraph<T> {

    public EventTransactionalIndexableGraph(final T baseIndexableGraph) {
        super(baseIndexableGraph);
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
