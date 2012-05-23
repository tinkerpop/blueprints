package com.tinkerpop.blueprints.util.wrappers.wrapped;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class WrappedVertexIterable implements CloseableIterable<Vertex> {

    private final Iterable<Vertex> iterable;

    public WrappedVertexIterable(final Iterable<Vertex> iterable) {
        this.iterable = iterable;
    }

    public Iterator<Vertex> iterator() {
        return new Iterator<Vertex>() {
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
        };
    }

    public void close() {
        if (this.iterable instanceof CloseableIterable) {
            ((CloseableIterable) iterable).close();
        }
    }
}