package com.tinkerpop.blueprints.pgm.impls.readonly.util;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.readonly.ReadOnlyTokens;
import com.tinkerpop.blueprints.pgm.impls.readonly.ReadOnlyVertex;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyVertexSequence implements CloseableSequence<Vertex> {

    private final Iterator<Vertex> itty;

    public ReadOnlyVertexSequence(final Iterator<Vertex> itty) {
        this.itty = itty;
    }

    public void remove() {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public Iterator<Vertex> iterator() {
        return this;
    }

    public Vertex next() {
        return new ReadOnlyVertex(itty.next());
    }

    public boolean hasNext() {
        return itty.hasNext();
    }

    public void close() {
        if (itty instanceof CloseableSequence) {
            ((CloseableSequence) itty).close();
        }
    }
}
