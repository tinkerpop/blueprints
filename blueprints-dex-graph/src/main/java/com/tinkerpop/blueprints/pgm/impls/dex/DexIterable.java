/**
 *
 */
package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * {@link Iterable} {@link com.tinkerpop.blueprints.pgm.impls.dex.DexElement} collection implementation for Dex.
 * <p/>
 * It is just a wrapper for Dex Objects class.
 * <p/>
 * This collections are registered into the {@link com.tinkerpop.blueprints.pgm.impls.dex.DexGraph} to be automatically
 * closed when the database is stopped.
 *
 * @param <T>
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class DexIterable<T extends Element> implements CloseableIterable<T> {

    private DexGraph graph;
    private com.sparsity.dex.gdb.Objects iterable;
    private Class<T> clazz;
    private final List<DexIterator> iterators = new ArrayList<DexIterator>();

    public DexIterable(final DexGraph g, final com.sparsity.dex.gdb.Objects iterable, final Class<T> clazz) {
        this.graph = g;
        this.iterable = iterable;
        this.clazz = clazz;
        this.graph.register(this);
    }

    @Override
    public Iterator<T> iterator() {
        final DexIterator itty = new DexIterator();
        this.iterators.add(itty);
        return itty;
    }

    /**
     * Close the collection closes iterators too.
     */
    public void close() {
        for (final DexIterator itty : iterators) {
            itty.close();
        }
        iterable.close();
        graph.unregister(this);
        iterable = null;
        graph = null;
    }


    private class DexIterator implements Iterator<T> {
        private com.sparsity.dex.gdb.ObjectsIterator itty = iterable.iterator();

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
                ret = (T) new DexVertex(graph, oid);
            } else if (clazz == Edge.class) {
                ret = (T) new DexEdge(graph, oid);
            } else if (clazz == Element.class) {
                ret = (T) new DexElement(graph, oid);
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