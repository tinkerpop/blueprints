package com.tinkerpop.blueprints.pgm.util.wrappers.event.util;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.EventEdge;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.GraphChangedListener;

import java.util.Iterator;
import java.util.List;

/**
 * A sequence of edges that applies the list of listeners into each edge.
 *
 * @author Stephen Mallette
 */
public class EventEdgeIterable implements CloseableIterable<Edge> {

    private final Iterable<Edge> iterable;
    private final List<GraphChangedListener> graphChangedListeners;

    public EventEdgeIterable(final Iterable<Edge> iterable, final List<GraphChangedListener> graphChangedListeners) {
        this.iterable = iterable;
        this.graphChangedListeners = graphChangedListeners;
    }

    public Iterator<Edge> iterator() {
        return new EventEdgeIterator();
    }

    public void close() {
        if (this.iterable instanceof CloseableIterable) {
            ((CloseableIterable) this.iterable).close();
        }
    }

    private class EventEdgeIterator implements Iterator<Edge> {

        private final Iterator<Edge> itty = iterable.iterator();

        public void remove() {
            this.itty.remove();
        }

        public Edge next() {
            return new EventEdge(this.itty.next(), graphChangedListeners);
        }

        public boolean hasNext() {
            return this.itty.hasNext();
        }


    }
}
