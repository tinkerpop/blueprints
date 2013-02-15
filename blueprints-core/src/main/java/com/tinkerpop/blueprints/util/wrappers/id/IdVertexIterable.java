package com.tinkerpop.blueprints.util.wrappers.id;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class IdVertexIterable implements CloseableIterable<Vertex> {
    private final Iterable<Vertex> iterable;
    private final IdGraph idGraph;

    public IdVertexIterable(final Iterable<Vertex> iterable, final IdGraph idGraph) {
        this.iterable = iterable;
        this.idGraph = idGraph;
    }

    public void close() {
        if (iterable instanceof CloseableIterable) {
            ((CloseableIterable) iterable).close();
        }
    }

    public Iterator<Vertex> iterator() {
        return new Iterator<Vertex>() {
            final Iterator<Vertex> itty = iterable.iterator();

            public boolean hasNext() {
                return itty.hasNext();
            }

            public Vertex next() {
                return new IdVertex(itty.next(), idGraph);
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
