package com.tinkerpop.blueprints.pgm.impls.wrapped;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedEdgeSequence implements CloseableSequence<Edge> {

    private final Iterator<Edge> itty;

    public WrappedEdgeSequence(final Iterator<Edge> itty) {
        this.itty = itty;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return this.itty.hasNext();
    }

    public Edge next() {
        return new WrappedEdge(itty.next());
    }

    public Iterator<Edge> iterator() {
        return this;
    }

    public void close() {
        if (this.itty instanceof CloseableSequence) {
            ((CloseableSequence) itty).close();
        }
    }
}