package com.tinkerpop.blueprints.pgm.util.wrappers.event.util;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.EventVertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.GraphChangedListener;

import java.util.Iterator;
import java.util.List;

/**
 * A sequence of vertices that applies the list of listeners into each vertex.
 *
 * @author Stephen Mallette
 */
public class EventVertexIterable implements CloseableIterable<Vertex> {

    private final Iterable<Vertex> iterable;
    private final List<GraphChangedListener> graphChangedListeners;

    public EventVertexIterable(final Iterable<Vertex> iterable, final List<GraphChangedListener> graphChangedListeners) {
        this.iterable = iterable;
        this.graphChangedListeners = graphChangedListeners;
    }

    public void close() {
        if (iterable instanceof CloseableIterable) {
            ((CloseableIterable) iterable).close();
        }
    }

    public Iterator<Vertex> iterator() {
        return new Iterator<Vertex>() {
            private final Iterator<Vertex> itty = iterable.iterator();

            public void remove() {
                this.itty.remove();
            }

            public Vertex next() {
                return new EventVertex(this.itty.next(), graphChangedListeners);
            }

            public boolean hasNext() {
                return this.itty.hasNext();
            }
        };
    }
}
