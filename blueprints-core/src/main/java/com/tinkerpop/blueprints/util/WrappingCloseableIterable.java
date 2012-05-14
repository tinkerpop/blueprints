package com.tinkerpop.blueprints.util;

import com.tinkerpop.blueprints.CloseableIterable;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappingCloseableIterable<T> implements CloseableIterable<T> {

    private final Iterable<T> iterable;

    public WrappingCloseableIterable(final Iterable<T> iterable) {
        this.iterable = iterable;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<T> itty = iterable.iterator();

            public void remove() {
                this.itty.remove();
            }

            public T next() {
                return this.itty.next();
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

    public String toString() {
        return this.iterable.toString();
    }
}
