package com.tinkerpop.blueprints.pgm.pipes;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author: Marko A. Rodriguez (http://markorodriguez.com)
 */
public class AggregatorPipe<S> extends AbstractPipe<S, S> {

    private final Collection<S> aggregate;
    private Iterator<S> aggregateIterator = null;

    public AggregatorPipe(final Collection<S> emptyCollection) {
        this.aggregate = emptyCollection;
    }

    public Collection<S> getAggregate() {
        return this.aggregate;
    }

    protected void setNext() {
        if (null == this.aggregateIterator) {
            while (this.starts.hasNext()) {
                aggregate.add(this.starts.next());
            }
            aggregateIterator = aggregate.iterator();
        }

        if (this.aggregateIterator.hasNext()) {
            this.nextEnd = this.aggregateIterator.next();
        } else {
            this.nextEnd = null;
        }

    }
}
