package com.tinkerpop.blueprints.util.wrappers.id;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;

import java.util.Iterator;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class IdEdgeIterable implements CloseableIterable<Edge> {
    private final Iterable<Edge> iterable;
    private final IdGraph idGraph;

    public IdEdgeIterable(Iterable<Edge> iterable, final IdGraph idGraph) {
        this.iterable = iterable;
        this.idGraph = idGraph;
    }

    public void close() {
        if (iterable instanceof CloseableIterable) {
            ((CloseableIterable) iterable).close();
        }
    }

    public Iterator<Edge> iterator() {
        return new Iterator<Edge>() {
            final Iterator<Edge> itty = iterable.iterator();

            public boolean hasNext() {
                return itty.hasNext();
            }

            public Edge next() {
                return new IdEdge(itty.next(), idGraph);
            }

            public void remove() {
                itty.remove();
            }
        };
    }
}
