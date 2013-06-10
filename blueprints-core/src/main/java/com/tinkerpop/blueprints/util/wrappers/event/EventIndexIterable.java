package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;

import java.util.Iterator;

/**
 * A sequence of indices that applies the list of listeners into each element.
 *
 * @author Stephen Mallette
 */
class EventIndexIterable<T extends Element> implements Iterable<Index<T>> {

    private final Iterable<Index<T>> iterable;
    private final EventGraph eventGraph;

    public EventIndexIterable(final Iterable<Index<T>> iterable, final EventGraph eventGraph) {
        this.iterable = iterable;
        this.eventGraph = eventGraph;
    }

    public Iterator<Index<T>> iterator() {
        return new Iterator<Index<T>>() {
            private final Iterator<Index<T>> itty = iterable.iterator();

            public void remove() {
                this.itty.remove();
            }

            public Index<T> next() {
                return new EventIndex<T>(this.itty.next(), eventGraph);
            }

            public boolean hasNext() {
                return itty.hasNext();
            }
        };
    }
}
