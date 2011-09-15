package com.tinkerpop.blueprints.pgm.impls;

import com.tinkerpop.blueprints.pgm.CloseableSequence;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappingCloseableSequence<T> implements CloseableSequence<T> {

    Iterator<T> itty;

    public WrappingCloseableSequence(Iterable<T> iterable) {
        this.itty = iterable.iterator();
    }

    public void remove() {
        this.itty.remove();
    }

    public T next() {
        return this.itty.next();
    }

    public boolean hasNext() {
        return this.itty.hasNext();
    }

    public Iterator<T> iterator() {
        return this;
    }

    public void close() {
        if (this.itty instanceof CloseableSequence) {
            ((CloseableSequence) itty).close();
        }
    }

    public String toString() {
        return this.itty.toString();
    }
}
