package com.tinkerpop.blueprints.impls.datomic;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import datomic.Datom;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicIterable<T extends Element> implements CloseableIterable<T> {

    private Iterable<Datom> datoms;
    private Collection<List<Object>> objects;
    private List<Object> ids;
    private final DatomicGraph graph;
    private Class<T> clazz;

    public DatomicIterable(final Iterable<Datom> datoms, final DatomicGraph graph, final Class<T> clazz) {
        this.graph = graph;
        this.datoms = datoms;
        this.clazz = clazz;
    }

    public DatomicIterable(final Collection<List<Object>> objects, final DatomicGraph graph, final Class<T> clazz) {
        this.graph = graph;
        this.objects = objects;
        this.clazz = clazz;
    }

    public DatomicIterable(final List<Object> ids, final DatomicGraph graph, final Class<T> clazz) {
        this.graph = graph;
        this.ids = ids;
        this.clazz = clazz;
    }

    @Override
    public Iterator<T> iterator() {
        if (datoms != null) {
            return new DatomicDatomIterator();
        }
        else {
            if (objects != null) {
                return new DatomicQueryIterator();
            }
            else {
                return new DatomicIdIterator();
            }
        }
    }

    @Override
    public void close() {
    }

    // Iterator for datoms
    private class DatomicDatomIterator implements Iterator<T> {
        private Iterator<Datom> iterator = datoms.iterator();

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            Object object = iterator.next().e();
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

    // Iterator for datomic query results
    private class DatomicQueryIterator implements Iterator<T> {
        private Iterator<List<Object>> iterator = objects.iterator();

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            Object object = iterator.next().get(0);
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

    // Iterator for datomic ids
    private class DatomicIdIterator implements Iterator<T> {
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