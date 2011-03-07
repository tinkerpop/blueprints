package com.tinkerpop.blueprints.pgm.impls.readonly.util;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.impls.readonly.ReadOnlyAutomaticIndex;
import com.tinkerpop.blueprints.pgm.impls.readonly.ReadOnlyIndex;
import com.tinkerpop.blueprints.pgm.impls.readonly.ReadOnlyTokens;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyIndexSequence<T extends Element> implements Iterator<Index<T>>, Iterable<Index<T>> {

    private final Iterator<Index<T>> itty;

    public ReadOnlyIndexSequence(final Iterator<Index<T>> itty) {
        this.itty = itty;
    }

    public void remove() {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public Iterator<Index<T>> iterator() {
        return this;
    }

    public Index<T> next() {
        final Index<T> index = this.itty.next();
        if (index.getIndexType().equals(Index.Type.MANUAL))
            return new ReadOnlyIndex<T>(index);
        else
            return new ReadOnlyAutomaticIndex<T>((AutomaticIndex<T>) index);
    }

    public boolean hasNext() {
        return itty.hasNext();
    }
}
