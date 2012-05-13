package com.tinkerpop.blueprints.pgm.util.wrappers.id;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Iterator;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdVertexIterable implements CloseableIterable<Vertex> {
    private final Iterable<Vertex> iterable;

    public IdVertexIterable(Iterable<Vertex> iterable) {
        this.iterable = iterable;
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
                return new IdVertex(itty.next());
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
