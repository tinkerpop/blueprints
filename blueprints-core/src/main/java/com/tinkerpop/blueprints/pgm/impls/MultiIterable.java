package com.tinkerpop.blueprints.pgm.impls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MultiIterable<S> implements Iterable<S> {

    private final List<Iterable<S>> iterables;

    public MultiIterable(final List<Iterable<S>> iterables) {
        this.iterables = iterables;
    }

    public Iterator<S> iterator() {
        if (this.iterables.size() == 0) {
            return new ArrayList<S>().iterator();
        } else {
            return new MultiIterator<S>(iterables);
        }
    }

    protected class MultiIterator<S> implements Iterator<S> {

        private final List<Iterator<S>> iterators = new ArrayList<Iterator<S>>();
        private int current = 0;

        public MultiIterator(final List<Iterable<S>> iterables) {
            for (final Iterable<S> iterable : iterables) {
                iterators.add(iterable.iterator());
            }
        }

        public void remove() {
            this.iterators.get(this.current).remove();
        }

        public boolean hasNext() {
            while (this.current < this.iterators.size()) {
                if (this.iterators.get(this.current).hasNext()) {
                    return true;
                }
                this.current++;
            }
            return false;
        }

        public S next() {
            while (this.current < this.iterators.size()) {
                final Iterator<S> temp = this.iterators.get(this.current);
                if (temp.hasNext()) {
                    return temp.next();
                }
                this.current++;
            }
            throw new NoSuchElementException();
        }
    }
}
