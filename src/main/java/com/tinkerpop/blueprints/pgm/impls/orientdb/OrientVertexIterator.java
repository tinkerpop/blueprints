package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphVertex;
import com.tinkerpop.blueprints.pgm.Vertex;

import java.util.Iterator;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientVertexIterator implements Iterator<Vertex>, Iterable<Vertex> {
    private Iterator<OGraphVertex> vertices;

    public OrientVertexIterator(final Iterator<OGraphVertex> vertices) {
        this.vertices = vertices;
    }

    public boolean hasNext() {
        return this.vertices.hasNext();
    }

    public Vertex next() {
        final OGraphVertex v = this.vertices.next();

        if (v == null)
            return null;

        return new OrientVertex(v);
    }

    public void remove() {
        this.vertices.remove();
    }

    public Iterator<Vertex> iterator() {
        return this;
    }
}
