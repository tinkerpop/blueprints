package com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.util;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.util.wrappers.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.EventTransactionalIndex;
import com.tinkerpop.blueprints.pgm.util.wrappers.eventtransactional.event.Event;

import java.util.Iterator;
import java.util.List;

/**
 * A sequence of indices that applies the list of listeners into each element.
 *
 * @author Stephen Mallette
 */
public class EventTransactionalIndexIterable<T extends Element> implements Iterable<Index<T>> {

    private final Iterable<Index<T>> iterable;
    private final List<GraphChangedListener> graphChangedListeners;
    private final ThreadLocal<List<Event>> eventBuffer;

    public EventTransactionalIndexIterable(final Iterable<Index<T>> iterable,
                                           final List<GraphChangedListener> graphChangedListeners,
                                           final ThreadLocal<List<Event>> eventBuffer) {
        this.iterable = iterable;
        this.graphChangedListeners = graphChangedListeners;
        this.eventBuffer = eventBuffer;
    }

    public Iterator<Index<T>> iterator() {
        return new Iterator<Index<T>>() {
            private final Iterator<Index<T>> itty = iterable.iterator();

            public void remove() {
                this.itty.remove();
            }

            public Index<T> next() {
                return new EventTransactionalIndex<T>(this.itty.next(), graphChangedListeners, eventBuffer);
            }

            public boolean hasNext() {
                return itty.hasNext();
            }
        };
    }
}
