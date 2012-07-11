package com.tinkerpop.blueprints.util.wrappers.event;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

import java.util.Iterator;
import java.util.List;

/**
 * A sequence of indices that applies the list of listeners into each element.
 *
 * @author Stephen Mallette
 */
class EventIndexIterable<T extends Element> implements Iterable<Index<T>> {

    private final Iterable<Index<T>> iterable;
    private final List<GraphChangedListener> graphChangedListeners;

    private final EventTrigger trigger;

    public EventIndexIterable(final Iterable<Index<T>> iterable, List<GraphChangedListener> graphChangedListeners,
                              final EventTrigger trigger) {
        this.iterable = iterable;
        this.graphChangedListeners = graphChangedListeners;
        this.trigger = trigger;
    }

    public Iterator<Index<T>> iterator() {
        return new Iterator<Index<T>>() {
            private final Iterator<Index<T>> itty = iterable.iterator();

            public void remove() {
                this.itty.remove();
            }

            public Index<T> next() {
                return new EventIndex<T>(this.itty.next(), graphChangedListeners, trigger);
            }

            public boolean hasNext() {
                return itty.hasNext();
            }
        };
    }
}
