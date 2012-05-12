package com.tinkerpop.blueprints.pgm.util.wrappers.readonly;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.util.wrappers.readonly.ReadOnlyIndex;
import com.tinkerpop.blueprints.pgm.util.wrappers.readonly.ReadOnlyTokens;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyIndexIterable<T extends Element> implements Iterable<Index<T>> {

    private final Iterable<Index<T>> iterable;

    public ReadOnlyIndexIterable(final Iterable<Index<T>> iterable) {
        this.iterable = iterable;
    }

    public Iterator<Index<T>> iterator() {
        return new Iterator<Index<T>>() {
            private final Iterator<Index<T>> itty = iterable.iterator();

            public void remove() {
                throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
            }

            public Index<T> next() {
                return new ReadOnlyIndex<T>(this.itty.next());
            }

            public boolean hasNext() {
                return this.itty.hasNext();
            }
        };
    }
}
