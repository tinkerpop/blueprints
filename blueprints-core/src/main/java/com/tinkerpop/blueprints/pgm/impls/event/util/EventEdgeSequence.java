package com.tinkerpop.blueprints.pgm.impls.event.util;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.event.EventEdge;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.Iterator;

public class EventEdgeSequence implements Iterator<Edge>, Iterable<Edge> {

    private final Iterator<Edge> itty;
    private final Iterator<GraphChangedListener> graphChangedListeners;

    public EventEdgeSequence(final Iterator<Edge> itty, final Iterator<GraphChangedListener> graphChangedListeners) {
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
}
