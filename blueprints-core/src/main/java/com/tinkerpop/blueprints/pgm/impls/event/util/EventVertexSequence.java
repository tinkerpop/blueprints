package com.tinkerpop.blueprints.pgm.impls.event.util;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.event.EventVertex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.Iterator;
import java.util.List;

/**
 * A sequence of vertices that applies the list of listeners into each vertex.
 *
 * @author Stephen Mallette
 */
public class EventVertexSequence implements CloseableSequence<Vertex> {

    private final Iterator<Vertex> itty;
    private final List<GraphChangedListener> graphChangedListeners;

    public EventVertexSequence(final Iterator<Vertex> itty, final List<GraphChangedListener> graphChangedListeners) {
        this.itty = itty;
        this.graphChangedListeners = graphChangedListeners;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Iterator<Vertex> iterator() {
        return this;
    }

    public Vertex next() {
        return new EventVertex(itty.next(), this.graphChangedListeners);
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
