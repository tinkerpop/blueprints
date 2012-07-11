package com.tinkerpop.blueprints.util.wrappers.partition;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class PartitionEdgeIterable implements CloseableIterable<Edge> {

    private final Iterable<Edge> iterable;
    private final PartitionGraph graph;

    public PartitionEdgeIterable(final Iterable<Edge> iterable, final PartitionGraph graph) {
        this.iterable = iterable;
        this.graph = graph;
    }

    public Iterator<Edge> iterator() {
        return new Iterator<Edge>() {
            private Iterator<Edge> itty = iterable.iterator();
            private PartitionEdge nextEdge;

            public void remove() {
                this.itty.remove();
            }

            public boolean hasNext() {
                if (null != this.nextEdge) {
                    return true;
                }
                while (this.itty.hasNext()) {
                    final Edge edge = this.itty.next();
                    if (graph.isInPartition(edge)) {
                        nextEdge = new PartitionEdge(edge, graph);
                        return true;
                    }
                }
                return false;

            }

            public Edge next() {
                if (null != this.nextEdge) {
                    final PartitionEdge temp = this.nextEdge;
                    this.nextEdge = null;
                    return temp;
                } else {
                    while (this.itty.hasNext()) {
                        final Edge edge = this.itty.next();
                        if (graph.isInPartition(edge)) {
                            return new PartitionEdge(edge, graph);
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