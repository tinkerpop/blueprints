package com.tinkerpop.blueprints.pgm.util.wrappers.partition.util;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.PartitionGraph;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.PartitionVertex;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionVertexSequence implements CloseableSequence<Vertex> {

    private final Iterator<Vertex> itty;
    private final PartitionGraph graph;
    private PartitionVertex nextVertex;

    public PartitionVertexSequence(final Iterator<Vertex> itty, final PartitionGraph graph) {
        this.itty = itty;
        this.graph = graph;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        if (null != this.nextVertex) {
            return true;
        }
        while (this.itty.hasNext()) {
            final Vertex vertex = this.itty.next();
            if (this.graph.isInPartition(vertex)) {
                this.nextVertex = new PartitionVertex(vertex, this.graph);
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
                if (this.graph.isInPartition(vertex)) {
                    return new PartitionVertex(vertex, this.graph);
                }
            }
            throw new NoSuchElementException();
        }
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
