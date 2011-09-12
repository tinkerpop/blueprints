package com.tinkerpop.blueprints.pgm.impls.event.util;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.impls.event.EventAutomaticIndex;
import com.tinkerpop.blueprints.pgm.impls.event.EventIndex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;

import java.util.Iterator;
import java.util.List;

/**
 * A sequence of indices that applies the list of listeners into each element.
 *
 * @author Stephen Mallette
 */
public class EventIndexSequence<T extends Element> implements Iterator<Index<T>>, Iterable<Index<T>> {

    private final Iterator<Index<T>> itty;
    private final List<GraphChangedListener> graphChangedListeners;

    public EventIndexSequence(final Iterator<Index<T>> itty, List<GraphChangedListener> graphChangedListeners) {
        this.itty = itty;
        this.graphChangedListeners = graphChangedListeners;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Iterator<Index<T>> iterator() {
        return this;
    }

    public Index<T> next() {
        final Index<T> index = this.itty.next();
        if (index.getIndexType().equals(Index.Type.MANUAL))
            return new EventIndex<T>(index, this.graphChangedListeners);
        else
            return new EventAutomaticIndex<T>((AutomaticIndex<T>) index, this.graphChangedListeners);
    }

    public boolean hasNext() {
        return itty.hasNext();
    }
}
