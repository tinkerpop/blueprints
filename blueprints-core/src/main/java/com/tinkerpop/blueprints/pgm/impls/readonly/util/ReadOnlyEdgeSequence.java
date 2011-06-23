package com.tinkerpop.blueprints.pgm.impls.readonly.util;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.readonly.ReadOnlyEdge;
import com.tinkerpop.blueprints.pgm.impls.readonly.ReadOnlyTokens;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyEdgeSequence implements CloseableSequence<Edge> {

    private final Iterator<Edge> itty;

    public ReadOnlyEdgeSequence(final Iterator<Edge> itty) {
        this.itty = itty;
    }

    public void remove() {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
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

    public void close() {
        if (itty instanceof CloseableSequence) {
            ((CloseableSequence) itty).close();
        }
    }
}
