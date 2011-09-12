package com.tinkerpop.blueprints.pgm.impls.event.util;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.event.EventEdge;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.Iterator;
import java.util.List;

/**
 * A sequence of edges that applies the list of listeners into each edge.
 *
 * @author Stephen Mallette
 */
public class EventEdgeSequence implements CloseableSequence<Edge> {

    private final Iterator<Edge> itty;
    private final List<GraphChangedListener> graphChangedListeners;

    public EventEdgeSequence(final Iterator<Edge> itty, final List<GraphChangedListener> graphChangedListeners) {
        this.itty = itty;
        this.graphChangedListeners = graphChangedListeners;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Iterator<Edge> iterator() {
        return this;
    }

    public Edge next() {
        return new EventEdge(itty.next(), this.graphChangedListeners);
    }

    public boolean hasNext() {
        return itty.hasNext();
    }

    public void close() {
        if (itty instanceof CloseableSequence) {
            ((CloseableSequence) itty).close();
        }
    }
}
