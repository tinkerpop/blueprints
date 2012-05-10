package com.tinkerpop.blueprints.pgm.util.wrappers.wrapped.util;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.wrapped.WrappedVertex;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedVertexIterable implements CloseableIterable<Vertex> {

    private final Iterable<Vertex> iterable;

    public WrappedVertexIterable(final Iterable<Vertex> iterable) {
        this.iterable = iterable;
    }

    public Iterator<Vertex> iterator() {
        return new WrappedVertexIterator();
    }

    public void close() {
        if (this.iterable instanceof CloseableIterable) {
            ((CloseableIterable) iterable).close();
        }
    }

    public class WrappedVertexIterator implements Iterator<Vertex> {
        private final Iterator<Vertex> itty = iterable.iterator();

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return this.itty.hasNext();
        }

        public Vertex next() {
            return new WrappedVertex(this.itty.next());
        }
    }
}