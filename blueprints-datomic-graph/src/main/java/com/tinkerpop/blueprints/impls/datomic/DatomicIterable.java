package com.tinkerpop.blueprints.impls.datomic;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import java.util.Iterator;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicIterable<T extends Element> implements CloseableIterable<T> {

    private final Iterable<Object> ids;
    private final DatomicGraph graph;
    private Class<T> clazz;

    public DatomicIterable(final Iterable<Object> ids, final DatomicGraph graph, final Class<T> clazz) {
        this.graph = graph;
        this.ids = ids;
        this.clazz = clazz;
    }

    @Override
    public Iterator<T> iterator() {
        return new DatomicIterator();
    }

    @Override
    public void close() {
    }

    private class DatomicIterator implements Iterator<T> {
        private Iterator<Object> iterator = ids.iterator();

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            Object object = iterator.next();
            T ret = null;
            if (clazz == Vertex.class) {
                ret = (T) new DatomicVertex(graph, object);
            } else if (clazz == Edge.class) {
                ret = (T) new DatomicEdge(graph, object);
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
}