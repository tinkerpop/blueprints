package com.tinkerpop.blueprints.pgm.impls.wrapped.util;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.impls.wrapped.WrappedAutomaticIndex;
import com.tinkerpop.blueprints.pgm.impls.wrapped.WrappedIndex;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedIndexSequence<T extends Element> implements Iterator<Index<T>>, Iterable<Index<T>> {

    protected Iterator<Index<T>> itty;

    public WrappedIndexSequence(final Iterator<Index<T>> itty) {
        this.itty = itty;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return this.itty.hasNext();
    }

    public Index<T> next() {
        final Index<T> index = itty.next();
        if (index.getIndexType().equals(Index.Type.MANUAL)) {
            return new WrappedIndex<T>(index);
        } else {
            return new WrappedAutomaticIndex<T>((AutomaticIndex<T>) index);
        }
    }

    public Iterator<Index<T>> iterator() {
        return this;
    }
}
