package com.tinkerpop.blueprints.pgm.util.wrappers.id.util;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.util.wrappers.id.IdEdge;

import java.util.Iterator;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdEdgeIterable implements Iterable<Edge> {
    private final Iterable<Edge> iterable;

    public IdEdgeIterable(Iterable<Edge> iterable) {
        this.iterable = iterable;
    }

    public Iterator<Edge> iterator() {
        final Iterator<Edge> itty = iterable.iterator();

        return new Iterator<Edge>() {
            public boolean hasNext() {
                return itty.hasNext();
            }

            public Edge next() {
                return new IdEdge(itty.next());
            }

            public void remove() {
                itty.remove();
            }
        };
    }
}
