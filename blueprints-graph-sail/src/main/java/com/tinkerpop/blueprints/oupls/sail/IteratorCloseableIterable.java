package com.tinkerpop.blueprints.oupls.sail;

import com.tinkerpop.blueprints.CloseableIterable;

import java.util.Iterator;

/**
 * A CloseableIterable which wraps a simple iterator.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IteratorCloseableIterable<T> implements CloseableIterable<T> {
    private final Iterator<T> iterator;

    public IteratorCloseableIterable(Iterator<T> iterator) {
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
}
