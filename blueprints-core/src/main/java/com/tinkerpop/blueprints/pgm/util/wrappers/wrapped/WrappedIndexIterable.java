package com.tinkerpop.blueprints.pgm.util.wrappers.wrapped;

import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Index;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedIndexIterable<T extends Element> implements Iterable<Index<T>> {

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
