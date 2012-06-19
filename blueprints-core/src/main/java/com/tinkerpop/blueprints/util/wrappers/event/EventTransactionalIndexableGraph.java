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

        // failure in this context doesn't mean Conclusion.FAILURE.  it means that the
        // state of closing out the transaction failed (in the setting of either one of
        // the available conclusions.  in other words, an exception occurred by calling
        // success/failure or a finishing of the transaction in any way.  if the transaction
        // was not a failure in that sense then events are fired (if success) and the buffer
        // is reset.
        boolean transactionFailure = false;
        try {
            this.baseGraph.stopTransaction(conclusion);
        } catch (RuntimeException re) {
            transactionFailure = true;
            throw re;
        } finally {
            if (!transactionFailure) {
                if (conclusion == Conclusion.SUCCESS) {
                    trigger.fireEventQueue();
                }

                this.trigger.resetEventQueue();
            }
        }
    }
}
