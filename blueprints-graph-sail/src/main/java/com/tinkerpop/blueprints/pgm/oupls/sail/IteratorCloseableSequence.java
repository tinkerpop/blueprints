package com.tinkerpop.blueprints.pgm.oupls.sail;

import com.tinkerpop.blueprints.pgm.CloseableSequence;

import java.util.Iterator;

/**
 * A CloseableSequence which wraps a simple iterator.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IteratorCloseableSequence<T> implements CloseableSequence<T> {
    private final Iterator<T> iterator;

    public IteratorCloseableSequence(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public void close() {
        // Do nothing.
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public T next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }
}
