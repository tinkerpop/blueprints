package com.tinkerpop.blueprints.pgm.impls.orientdb.util;

import com.orientechnologies.orient.core.db.graph.OGraphEdge;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientEdge;

import java.util.Iterator;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientEdgeSequence implements Iterator<Edge>, Iterable<Edge> {
    private final Iterator<OGraphEdge> edges;


    public OrientEdgeSequence(final Iterator<OGraphEdge> edges) {
        this.edges = edges;
    }

    public boolean hasNext() {
        return this.edges.hasNext();
    }

    public Edge next() {
        return new OrientEdge(this.edges.next());
    }

    public void remove() {
        this.edges.remove();
    }

    public Iterator<Edge> iterator() {
        return this;
    }
}
