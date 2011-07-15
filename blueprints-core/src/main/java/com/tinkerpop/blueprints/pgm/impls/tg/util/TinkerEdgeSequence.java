package com.tinkerpop.blueprints.pgm.impls.tg.util;

import com.tinkerpop.blueprints.pgm.Edge;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerEdgeSequence implements Iterable<Edge>, Iterator<Edge> {

    private final Iterator<Edge> itty;
    private final String label;
    private Edge currentEdge;

    public TinkerEdgeSequence(final Collection<Edge> edges, final String label) {
        this.itty = edges.iterator();
        this.label = label;
    }


    public Iterator<Edge> iterator() {
        return this;
    }

    public Edge next() {
        if (null != this.currentEdge) {
            Edge temp = this.currentEdge;
            this.currentEdge = null;
            return temp;
        } else {
            while (true) {
                final Edge edge = this.itty.next();
                if (edge.getLabel().equals(this.label)) {
                    return edge;
                }
            }
        }

    }

    public boolean hasNext() {
        if (null != this.currentEdge) {
            return true;
        } else {
            while (this.itty.hasNext()) {
                final Edge edge = itty.next();
                if (edge.getLabel().equals(this.label)) {
                    this.currentEdge = edge;
                    return true;
                }
            }
            return false;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
