package com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.util;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.EventTransactionalVertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.Event;

import java.util.Iterator;
import java.util.List;

/**
 * A sequence of vertices that applies the list of listeners into each vertex.
 *
 * @author Stephen Mallette
 */
public class EventTransactionalVertexIterable implements CloseableIterable<Vertex> {

    private final Iterable<Vertex> iterable;
    private final List<GraphChangedListener> graphChangedListeners;
    private final ThreadLocal<List<Event>> eventBuffer;

    public EventTransactionalVertexIterable(final Iterable<Vertex> iterable,
                                            final List<GraphChangedListener> graphChangedListeners,
                                            final ThreadLocal<List<Event>> eventBuffer) {
        this.iterable = iterable;
        this.graphChangedListeners = graphChangedListeners;
        this.eventBuffer = eventBuffer;
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
                return new EventTransactionalVertex(this.itty.next(), graphChangedListeners, eventBuffer);
            }

            public boolean hasNext() {
                return this.itty.hasNext();
            }
        };
    }
}
