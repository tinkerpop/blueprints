package com.tinkerpop.blueprints.pgm.util.wrappers.partition.util;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.PartitionEdge;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.PartitionGraph;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionEdgeSequence implements CloseableSequence<Edge> {

    private final Iterator<Edge> itty;
    private final PartitionGraph graph;
    private PartitionEdge nextEdge;

    public PartitionEdgeSequence(final Iterator<Edge> itty, final PartitionGraph graph) {
        this.itty = itty;
        this.graph = graph;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        if (null != this.nextEdge) {
            return true;
        }
        while (this.itty.hasNext()) {
            final Edge edge = this.itty.next();
            if (this.graph.isInPartition(edge)) {
                nextEdge = new PartitionEdge(edge, this.graph);
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
                if (this.graph.isInPartition(edge)) {
                    return new PartitionEdge(edge, this.graph);
                }
            }
            throw new NoSuchElementException();
        }
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