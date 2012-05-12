package com.tinkerpop.blueprints.pgm.util.wrappers.readonly;

import com.tinkerpop.blueprints.pgm.CloseableIterable;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.util.wrappers.readonly.ReadOnlyEdge;
import com.tinkerpop.blueprints.pgm.util.wrappers.readonly.ReadOnlyTokens;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyEdgeIterable implements CloseableIterable<Edge> {

    private final Iterable<Edge> iterable;

    public ReadOnlyEdgeIterable(final Iterable<Edge> iterable) {
        this.iterable = iterable;
    }

    public Iterator<Edge> iterator() {
        return new Iterator<Edge>() {
            private final Iterator<Edge> itty = iterable.iterator();

            public void remove() {
                throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
            }

            public Edge next() {
                return new ReadOnlyEdge(this.itty.next());
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
