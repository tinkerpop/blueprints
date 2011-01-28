package com.tinkerpop.blueprints.pgm.oupls.sail;

import java.util.Iterator;

/**
 * An Iterator which constrains results based on a specified criterion.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class FilteredIterator<T> implements Iterator<T> {
    private final Iterator<T> baseIterator;
    private final Criterion<T> criterion;
    private T cur;

    /**
     * Create a new iterator which is constrained by the given criterion.
     *
     * @param baseIterator a lower-level iterator of untested values
     * @param criterion    only values which pass this criterion will be accessible through the iterator
     */
    public FilteredIterator(final Iterator<T> baseIterator, final Criterion<T> criterion) {
        this.baseIterator = baseIterator;
        this.criterion = criterion;
        advanceToNext();
    }

    public boolean hasNext() {
        return null != cur;
    }

    public T next() {
        T t = cur;
        advanceToNext();
        return t;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void advanceToNext() {
        while (baseIterator.hasNext()) {
            cur = baseIterator.next();
            if (criterion.fulfilledBy(cur)) {
                return;
            }
        }

        cur = null;
    }

    public interface Criterion<T> {
        boolean fulfilledBy(T t);
    }
}