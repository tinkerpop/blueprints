package com.tinkerpop.blueprints.pgm.impls.wrapped.util;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.wrapped.WrappedVertex;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedVertexSequence implements CloseableSequence<Vertex> {

    private final Iterator<Vertex> itty;

    public WrappedVertexSequence(final Iterator<Vertex> itty) {
        this.itty = itty;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        return this.itty.hasNext();
    }

    public Vertex next() {
        return new WrappedVertex(itty.next());
    }

    public Iterator<Vertex> iterator() {
        return this;
    }

    public void close() {
        if (this.itty instanceof CloseableSequence) {
            ((CloseableSequence) itty).close();
        }
    }
}
