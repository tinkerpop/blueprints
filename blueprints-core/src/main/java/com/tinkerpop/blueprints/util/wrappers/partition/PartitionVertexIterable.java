package com.tinkerpop.blueprints.util.wrappers.partition;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Vertex;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class PartitionVertexIterable implements CloseableIterable<Vertex> {

    private final Iterable<Vertex> iterable;
    private final PartitionGraph graph;


    public PartitionVertexIterable(final Iterable<Vertex> iterable, final PartitionGraph graph) {
        this.iterable = iterable;
        this.graph = graph;
    }

    public Iterator<Vertex> iterator() {
        return new Iterator<Vertex>() {
            private final Iterator<Vertex> itty = iterable.iterator();
            private PartitionVertex nextVertex;

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                if (null != this.nextVertex) {
                    return true;
                }
                while (this.itty.hasNext()) {
                    final Vertex vertex = this.itty.next();
                    if (graph.isInPartition(vertex)) {
                        this.nextVertex = new PartitionVertex(vertex, graph);
                        return true;
                    }
                }
                return false;

            }

            public Vertex next() {
                if (null != this.nextVertex) {
                    final PartitionVertex temp = this.nextVertex;
                    this.nextVertex = null;
                    return temp;
                } else {
                    while (this.itty.hasNext()) {
                        final Vertex vertex = this.itty.next();
                        if (graph.isInPartition(vertex)) {
                            return new PartitionVertex(vertex, graph);
                        }
                    }
                    throw new NoSuchElementException();
                }
            }
        };
    }

    public void close() {
        if (this.iterable instanceof CloseableIterable) {
            ((CloseableIterable) iterable).close();
        }
    }
}
