package com.tinkerpop.blueprints.pgm.impls.named.util;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.named.NamedGraph;
import com.tinkerpop.blueprints.pgm.impls.named.NamedVertex;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NamedVertexSequence implements CloseableSequence<Vertex> {

    private final Iterator<Vertex> itty;
    private final NamedGraph graph;
    private NamedVertex nextVertex;

    public NamedVertexSequence(final Iterator<Vertex> itty, final NamedGraph graph) {
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
            if (this.graph.isInGraph(vertex)) {
                nextVertex = new NamedVertex(vertex, this.graph);
                return true;
            }
        }
        return false;

    }

    public Vertex next() {
        if (null != this.nextVertex) {
            final NamedVertex temp = this.nextVertex;
            this.nextVertex = null;
            return temp;
        } else {
            while (this.itty.hasNext()) {
                final Vertex vertex = this.itty.next();
                if (this.graph.isInGraph(vertex)) {
                    return new NamedVertex(vertex, this.graph);
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
