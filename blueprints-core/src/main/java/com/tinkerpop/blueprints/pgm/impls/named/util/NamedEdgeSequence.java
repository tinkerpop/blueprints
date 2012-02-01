package com.tinkerpop.blueprints.pgm.impls.named.util;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.named.NamedEdge;
import com.tinkerpop.blueprints.pgm.impls.named.NamedGraph;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NamedEdgeSequence implements CloseableSequence<Edge> {

    private final Iterator<Edge> itty;
    private final NamedGraph graph;
    private NamedEdge nextEdge;

    public NamedEdgeSequence(final Iterator<Edge> itty, final NamedGraph graph) {
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
            if (this.graph.isInGraph(edge)) {
                nextEdge = new NamedEdge(edge, this.graph);
                return true;
            }
        }
        return false;

    }

    public Edge next() {
        if (null != this.nextEdge) {
            final NamedEdge temp = this.nextEdge;
            this.nextEdge = null;
            return temp;
        } else {
            while (this.itty.hasNext()) {
                final Edge edge = this.itty.next();
                if (this.graph.isInGraph(edge)) {
                    return new NamedEdge(edge, this.graph);
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