/**
 *
 */
package com.tinkerpop.blueprints.impls.sparksee;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * {@link Iterable} {@link SparkseeElement} collection implementation for Sparksee.
 * <p/>
 * It is just a wrapper for Sparksee Objects class.
 * <p/>
 * This collections are registered into the {@link SparkseeGraph} to be automatically
 * closed when the database is stopped.
 *
 * @param <T>
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
class SparkseeIterable<T extends Element> implements CloseableIterable<T> {

    private SparkseeGraph graph;
    private com.sparsity.sparksee.gdb.Objects iterable;
    private Class<T> clazz;
    private final List<SparkseeIterator> iterators = new ArrayList<SparkseeIterator>();

    public SparkseeIterable(final SparkseeGraph g, final com.sparsity.sparksee.gdb.Objects iterable, final Class<T> clazz) {
        this.graph = g;
        this.iterable = iterable;
        this.clazz = clazz;
        this.graph.register(this);
    }

    @Override
    public Iterator<T> iterator() {
        final SparkseeIterator itty = new SparkseeIterator();
        this.iterators.add(itty);
        return itty;
    }

    /**
     * Close the collection closes iterators too.
     */
    public void close() {
        close(true);
    }

    void close(boolean unregister) {
        for (final SparkseeIterator itty : iterators) {
            itty.close();
        }
        iterable.close();
        if (unregister) {
            graph.unregister(this);
        }
        iterable = null;
        graph = null;
    }

    private class SparkseeIterator implements Iterator<T> {
        private com.sparsity.sparksee.gdb.ObjectsIterator itty = iterable.iterator();

        @Override
        public boolean hasNext() {
            return itty.hasNext();
        }

        @Override
        public T next() {
            long oid = itty.next();
            if (oid == -1)
                throw new NoSuchElementException();
            T ret = null;
            if (clazz == Vertex.class) {
                ret = (T) new SparkseeVertex(graph, oid);
            } else if (clazz == Edge.class) {
                ret = (T) new SparkseeEdge(graph, oid);
            } else if (clazz == Element.class) {
                ret = (T) new SparkseeElement(graph, oid);
            } else {
                throw new IllegalStateException();
            }
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void close() {
            itty.close();
        }
    }
}