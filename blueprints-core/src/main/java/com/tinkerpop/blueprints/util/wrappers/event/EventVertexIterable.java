package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;

/**
 * A sequence of vertices that applies the list of listeners into each vertex.
 *
 * @author Stephen Mallette
 */
public class EventVertexIterable implements CloseableIterable<Vertex> {

    private final Iterable<Vertex> iterable;
    private final EventGraph eventGraph;

    public EventVertexIterable(final Iterable<Vertex> iterable, final EventGraph eventGraph) {
        this.iterable = iterable;
        this.eventGraph = eventGraph;
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
                return new EventVertex(this.itty.next(), eventGraph);
            }

            public boolean hasNext() {
                return this.itty.hasNext();
            }
        };
    }
}
