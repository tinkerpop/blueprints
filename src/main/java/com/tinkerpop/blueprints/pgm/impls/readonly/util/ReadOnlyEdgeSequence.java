package com.tinkerpop.blueprints.pgm.impls.readonly.util;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.readonly.ReadOnlyEdge;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyEdgeSequence implements Iterator<Edge>, Iterable<Edge> {

    private final Iterator<Edge> itty;

    public ReadOnlyEdgeSequence(final Iterator<Edge> itty) {
        this.itty = itty;
    }

    public void remove() {
        this.itty.remove();
    }

    public Iterator<Edge> iterator() {
        return this;
    }

    public Edge next() {
        return new ReadOnlyEdge(itty.next());
    }

    public boolean hasNext() {
        return itty.hasNext();
    }
}
