package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphEdge;
import com.tinkerpop.blueprints.pgm.Edge;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientEdgeIterator implements Iterator<Edge>, Iterable<Edge> {
    private final Iterator<OGraphEdge> edges;


    public OrientEdgeIterator(final Iterator<OGraphEdge> edges) {
        this.edges = edges;
    }

    public boolean hasNext() {
        return this.edges.hasNext();
    }

    public Edge next() {
        final OGraphEdge e = this.edges.next();

        if (e == null)
           throw new NoSuchElementException();

        return new OrientEdge(e);
    }

    public void remove() {
        edges.remove();
    }

    public Iterator<Edge> iterator() {
        return this;
    }
}
