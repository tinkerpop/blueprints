package com.tinkerpop.blueprints.pgm.util.wrappers.id;

import com.tinkerpop.blueprints.pgm.Edge;

import java.util.Iterator;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class IdEdgeIterable implements Iterable<Edge> {
    private final Iterable<Edge> base;

    public IdEdgeIterable(Iterable<Edge> base) {
        this.base = base;
    }

    public Iterator<Edge> iterator() {
        final Iterator<Edge> baseIter = base.iterator();

        return new Iterator<Edge>() {
            public boolean hasNext() {
                return baseIter.hasNext();
            }

            public Edge next() {
                return new IdEdge(baseIter.next());
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
