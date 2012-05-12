package com.tinkerpop.blueprints.pgm.util.wrappers.readonly.util;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.readonly.ReadOnlyTokens;
import com.tinkerpop.blueprints.pgm.util.wrappers.readonly.ReadOnlyVertex;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyVertexIterable implements CloseableIterable<Vertex> {

    private final Iterable<Vertex> iterable;

    public ReadOnlyVertexIterable(final Iterable<Vertex> iterable) {
        this.iterable = iterable;
    }

    public Iterator<Vertex> iterator() {
        return new Iterator<Vertex>() {
            private Iterator<Vertex> itty = iterable.iterator();

            public void remove() {
                throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
            }


            public Vertex next() {
                return new ReadOnlyVertex(this.itty.next());
            }

            public boolean hasNext() {
                return this.itty.hasNext();
            }
        };
    }

    public void close() {
        if (this.iterable instanceof CloseableIterable) {
            ((CloseableIterable) this.iterable).close();
        }
    }
}
