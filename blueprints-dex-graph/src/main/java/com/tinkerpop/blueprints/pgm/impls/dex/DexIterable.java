/**
 *
 */
package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;
import edu.upc.dama.dex.core.Objects;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * {@link Iterable} {@link com.tinkerpop.blueprints.pgm.impls.dex.DexElement} collection implementation for DEX.
 * <p/>
 * It is just a wrapper for DEX Objects class.
 * <p/>
 * This collections are registered into the {@link com.tinkerpop.blueprints.pgm.impls.dex.DexGraph} to be automatically
 * closed when the database is stopped.
 *
 * @param <T>
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class DexIterable<T extends Element> implements Iterable<T> {

    private DexGraph graph = null;
    private Objects objs = null;
    private Class<T> clazz = null;

    public DexIterable(final DexGraph g, final Objects objs, final Class<T> clazz) {
        this.graph = g;
        this.objs = objs;
        this.clazz = clazz;

        this.graph.register(this);
    }

    @Override
    public Iterator<T> iterator() {
        return new DEXIterator<T>(objs.iterator());
    }

    /**
     * Close the collection closes iterators too.
     */
    public void close() {
        objs.close(); // this closes DEX Objects.Iterator instances
        graph.unregister(this);

        objs = null;
        graph = null;
    }

    /**
     * {@link com.tinkerpop.blueprints.pgm.impls.dex.DexElement} {@link Iterator} implementation for DEX.
     *
     * @param <TT>
     * @author <a href="http://www.sparsity-technologies.com">Sparsity
     *         Technologies</a>
     */
    public class DEXIterator<TT extends Element> implements Iterator<TT> {

        private Objects.Iterator it = null;

        DEXIterator(final Objects.Iterator it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public TT next() {
            long oid = it.next();
            if (oid == -1)
                throw new NoSuchElementException();
            TT ret = null;
            if (clazz == Vertex.class) {
                ret = (TT) new DexVertex(graph, oid);
            } else if (clazz == Edge.class) {
                ret = (TT) new DexEdge(graph, oid);
            } else if (clazz == Element.class) {
                ret = (TT) new DexElement(graph, oid);
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
            it.close();
            it = null;
        }

    }

}
