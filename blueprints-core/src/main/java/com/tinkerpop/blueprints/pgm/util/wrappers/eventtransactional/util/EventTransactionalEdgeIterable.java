package com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.util;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.EventTransactionalEdge;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.Event;

import java.util.Iterator;
import java.util.List;

/**
 * A sequence of edges that applies the list of listeners into each edge.
 *
 * @author Stephen Mallette
 */
public class EventTransactionalEdgeIterable implements CloseableIterable<Edge> {

    private final Iterable<Edge> iterable;
    private final List<GraphChangedListener> graphChangedListeners;
    private final ThreadLocal<List<Event>> eventBuffer;

    public EventTransactionalEdgeIterable(final Iterable<Edge> iterable,
                                          final List<GraphChangedListener> graphChangedListeners,
                                          final ThreadLocal<List<Event>> eventBuffer) {
        this.iterable = iterable;
        this.graphChangedListeners = graphChangedListeners;
        this.eventBuffer = eventBuffer;
    }

    public Iterator<Edge> iterator() {
        return new Iterator<Edge>() {
            private final Iterator<Edge> itty = iterable.iterator();

            public void remove() {
                this.itty.remove();
            }

            public Edge next() {
                return new EventTransactionalEdge(this.itty.next(), graphChangedListeners, eventBuffer);
            }

            public boolean hasNext() {
                return this.itty.hasNext();
            }
        };
    }

    public void close() {
        if (this.iterable instanceof CloseableIterable) {
            ((CloseableIterable) this.iterable).close();
        }
    }
}
