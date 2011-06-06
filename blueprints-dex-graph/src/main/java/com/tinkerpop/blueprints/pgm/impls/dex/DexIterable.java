/**
 *
 */
package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
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
public class DexIterable<T extends Element> implements CloseableSequence<T> {

    private DexGraph graph;
    private Objects objs;
    private Class<T> clazz;
    private Objects.Iterator itty;

    public DexIterable(final DexGraph g, final Objects objs, final Class<T> clazz) {
        this.graph = g;
        this.objs = objs;
        this.clazz = clazz;
        this.itty = objs.iterator();
        this.graph.register(this);
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }

    /**
     * Close the collection closes iterators too.
     */
    public void close() {
        itty.close();
        objs.close(); // this closes DEX Objects.Iterator instances
        graph.unregister(this);

        objs = null;
        graph = null;
    }

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
}
