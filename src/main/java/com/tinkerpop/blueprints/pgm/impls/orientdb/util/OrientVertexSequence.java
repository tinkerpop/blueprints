package com.tinkerpop.blueprints.pgm.impls.orientdb.util;

import com.orientechnologies.orient.core.db.graph.OGraphVertex;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientVertex;

import java.util.Iterator;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientVertexSequence implements Iterator<Vertex>, Iterable<Vertex> {
    private Iterator<OGraphVertex> vertices;

    public OrientVertexSequence(final Iterator<OGraphVertex> vertices) {
        this.vertices = vertices;
    }

    public boolean hasNext() {
        return this.vertices.hasNext();
    }

    public Vertex next() {
        return new OrientVertex(this.vertices.next());
    }

    public void remove() {
        this.vertices.remove();
    }

    public Iterator<Vertex> iterator() {
        return this;
    }
}
