package com.tinkerpop.blueprints.pgm.pipes.sefs;

import com.tinkerpop.blueprints.pgm.pipes.AbstractPipe;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class AggregatorPipe<S> extends AbstractPipe<S, S> implements SideEffectPipe<S, S, Collection<S>> {

    private final Collection<S> aggregate;
    private Iterator<S> aggregateIterator = null;

    public AggregatorPipe(final Collection<S> collection) {
        this.aggregate = collection;
    }

    protected S processNextStart() {
        if (null == this.aggregateIterator) {
            while (this.starts.hasNext()) {
                aggregate.add(this.starts.next());
            }
            aggregateIterator = aggregate.iterator();
        }
        return this.aggregateIterator.next();
    }

    public Collection<S> getSideEffect() {
        return this.aggregate;
    }
}
