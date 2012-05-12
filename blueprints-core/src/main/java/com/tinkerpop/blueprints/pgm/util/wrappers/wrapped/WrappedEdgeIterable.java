package com.tinkerpop.blueprints.pgm.util.wrappers.wrapped;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.util.wrappers.wrapped.WrappedEdge;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedEdgeIterable implements CloseableIterable<Edge> {

    private final Iterable<Edge> iterable;

    public WrappedEdgeIterable(final Iterable<Edge> iterable) {
        this.iterable = iterable;
    }

    public Iterator<Edge> iterator() {
        return new Iterator<Edge>() {
            private final Iterator<Edge> itty = iterable.iterator();

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return this.itty.hasNext();
            }

            public Edge next() {
                return new WrappedEdge(this.itty.next());
            }
        };
    }

    public void close() {
        if (this.iterable instanceof CloseableIterable) {
            ((CloseableIterable) iterable).close();
        }
    }
}