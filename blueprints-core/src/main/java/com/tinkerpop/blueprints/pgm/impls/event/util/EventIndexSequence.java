package com.tinkerpop.blueprints.pgm.impls.event.util;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.impls.event.EventAutomaticIndex;
import com.tinkerpop.blueprints.pgm.impls.event.EventIndex;
import com.tinkerpop.blueprints.pgm.impls.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.pgm.impls.readonly.ReadOnlyAutomaticIndex;
import com.tinkerpop.blueprints.pgm.impls.readonly.ReadOnlyIndex;
import com.tinkerpop.blueprints.pgm.impls.readonly.ReadOnlyTokens;

import java.util.Iterator;

/**
 * A sequence of indices that applies the list of listeners into each element.
 */
public class EventIndexSequence<T extends Element> implements Iterator<Index<T>>, Iterable<Index<T>> {

    private final Iterator<Index<T>> itty;
    private final Iterator<GraphChangedListener> graphChangedListeners;

    public EventIndexSequence(final Iterator<Index<T>> itty, Iterator<GraphChangedListener> graphChangedListeners) {
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
            return new EventIndex<T>(index, graphChangedListeners);
        else
            return new EventAutomaticIndex<T>((AutomaticIndex<T>) index, graphChangedListeners);
    }

    public boolean hasNext() {
        return itty.hasNext();
    }
}
