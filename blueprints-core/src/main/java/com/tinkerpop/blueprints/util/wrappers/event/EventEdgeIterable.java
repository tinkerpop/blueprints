package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

import java.util.Iterator;
import java.util.List;

/**
 * A sequence of edges that applies the list of listeners into each edge.
 *
 * @author Stephen Mallette
 */
class EventEdgeIterable implements CloseableIterable<Edge> {

    private final Iterable<Edge> iterable;
    private final List<GraphChangedListener> graphChangedListeners;

    private final EventTrigger trigger;

    public EventEdgeIterable(final Iterable<Edge> iterable, final List<GraphChangedListener> graphChangedListeners,
                             final EventTrigger trigger) {
        this.iterable = iterable;
        this.graphChangedListeners = graphChangedListeners;
        this.trigger = trigger;
    }

    public Iterator<Edge> iterator() {
        return new Iterator<Edge>() {
            private final Iterator<Edge> itty = iterable.iterator();

            public void remove() {
                this.itty.remove();
            }

            public Edge next() {
                return new EventEdge(this.itty.next(), graphChangedListeners, trigger);
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
