package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

/**
 * The transactional and indexable implementation of EventGraph where events are raised in batch in the order they
 * changes occured to the graph, but only after a successful commit to the underlying graph.
 *
 * @author Stephen Mallette
 */
public class EventTransactionalIndexableGraph<T extends IndexableGraph & TransactionalGraph> extends EventIndexableGraph<T>
        implements TransactionalGraph, IndexableGraph, WrapperGraph<T> {

    public EventTransactionalIndexableGraph(final T baseIndexableGraph) {
        super(baseIndexableGraph);
        this.trigger = new EventTrigger(this, true);
    }


    @Override
    public void stopTransaction(Conclusion conclusion) {
        if (Conclusion.SUCCESS == conclusion)
            commit();
        else
            rollback();
    }

    /**
     * A commit only fires the event queue on successful operation.  If the commit operation to the underlying
     * graph fails, the event queue will not fire and the queue will not be reset.
     */
    public void commit() {
        boolean transactionFailure = false;
        try {
            this.baseGraph.commit();
        } catch (RuntimeException re) {
            transactionFailure = true;
            throw re;
        } finally {
            if (!transactionFailure) {
                trigger.fireEventQueue();
                trigger.resetEventQueue();
            }
        }
    }

    /**
     * A rollback only resets the event queue on successful operation.  If the rollback operation to the underlying
     * graph fails, the event queue will not be reset.
     */
    public void rollback() {
        boolean transactionFailure = false;
        try {
            this.baseGraph.rollback();
        } catch (RuntimeException re) {
            transactionFailure = true;
            throw re;
        } finally {
            if (!transactionFailure) {
                trigger.resetEventQueue();
            }
        }
    }
}
