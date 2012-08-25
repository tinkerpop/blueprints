package com.tinkerpop.blueprints.oupls.sail;

import com.tinkerpop.blueprints.CloseableIterable;

import java.util.Iterator;
import java.util.List;

/**
 * An Iterator which constrains results based on a specified criterion.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class FilteredIterator<T> implements CloseableIterable<T>, Iterator<T> {
    private final Iterator<T> baseIterator;
    private final Criterion<T> criterion;
    private T cur;

    /**
     * Create a new iterator which is constrained by the given criterion.
     *
     * @param base      a lower-level iterator of untested values
     * @param criterion only values which pass this criterion will be accessible through the iterator
     */
    public FilteredIterator(final Iterable<T> base, final Criterion<T> criterion) {
        this.baseIterator = base.iterator();
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

    @Override
    public void close() {
        // Do nothing.
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }

    public static interface Criterion<T> {
        boolean fulfilledBy(T t);
    }

    public static class CompoundCriterion<T> implements Criterion<T> {
        private final List<Criterion<T>> criteria;

        public CompoundCriterion(List<Criterion<T>> criteria) {
            this.criteria = criteria;
        }

        public boolean fulfilledBy(T t) {
            for (Criterion<T> c : criteria) {
                if (!c.fulfilledBy(t)) {
                    return false;
                }
            }

            return true;
        }
    }
}