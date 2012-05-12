package com.tinkerpop.blueprints.pgm.util.wrappers.id.util;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.id.IdVertex;

import java.util.Iterator;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdVertexIterable implements Iterable<Vertex> {
    private final Iterable<Vertex> iterable;

    public IdVertexIterable(Iterable<Vertex> iterable) {
        this.iterable = iterable;
    }

    public Iterator<Vertex> iterator() {
        final Iterator<Vertex> itty = iterable.iterator();

        return new Iterator<Vertex>() {
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
