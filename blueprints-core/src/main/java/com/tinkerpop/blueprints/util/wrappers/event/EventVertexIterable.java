package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

import java.util.Iterator;
import java.util.List;

/**
 * A sequence of vertices that applies the list of listeners into each vertex.
 *
 * @author Stephen Mallette
 */
class EventVertexIterable implements CloseableIterable<Vertex> {

    private final Iterable<Vertex> iterable;
    private final List<GraphChangedListener> graphChangedListeners;

    private final EventTrigger trigger;

    public EventVertexIterable(final Iterable<Vertex> iterable, final List<GraphChangedListener> graphChangedListeners,
                               final EventTrigger trigger) {
        this.iterable = iterable;
        this.graphChangedListeners = graphChangedListeners;
        this.trigger = trigger;
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
                return new EventVertex(this.itty.next(), graphChangedListeners, trigger);
            }

            public boolean hasNext() {
                return this.itty.hasNext();
            }
        };
    }
}
