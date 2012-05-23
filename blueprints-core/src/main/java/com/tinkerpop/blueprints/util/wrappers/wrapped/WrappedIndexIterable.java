package com.tinkerpop.blueprints.util.wrappers.wrapped;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class WrappedIndexIterable<T extends Element> implements Iterable<Index<T>> {

    private Iterable<Index<T>> iterable;

    public WrappedIndexIterable(final Iterable<Index<T>> iterable) {
        this.iterable = iterable;
    }

    public Iterator<Index<T>> iterator() {
        return new Iterator<Index<T>>() {

            private final Iterator<Index<T>> itty = iterable.iterator();

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return this.itty.hasNext();
            }

            public Index<T> next() {
                return new WrappedIndex<T>(this.itty.next());
            }
        };
    }
}
