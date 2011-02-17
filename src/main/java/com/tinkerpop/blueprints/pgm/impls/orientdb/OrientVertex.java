package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

import java.util.Collections;
import java.util.Set;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientVertex extends OrientElement implements Vertex {

    public OrientVertex(final OrientGraph iGraph, final ODocument rawVertex) {
        super(iGraph, rawVertex);
    }

    public Iterable<Edge> getOutEdges() {
        return getOutEdges(null);
    }

    public Iterable<Edge> getInEdges() {
        return getInEdges(null);
    }

    public Iterable<Edge> getOutEdges(final String label) {
        if (this.rawElement == null)
            return Collections.emptyList();

        return new OrientElementSequence<Edge>(graph, graph.getRawGraph().getOutEdges(this.rawElement, label).iterator());
    }

    public Iterable<Edge> getInEdges(final String label) {
        if (this.rawElement == null)
            return Collections.emptyList();

        return new OrientElementSequence<Edge>(graph, graph.getRawGraph().getInEdges(this.rawElement, label).iterator());
    }

    public Set<String> getPropertyKeys() {
        final Set<String> set = super.getPropertyKeys();
        if (set.size() > 0) {
            set.remove(OGraphDatabase.VERTEX_FIELD_IN_EDGES);
            set.remove(OGraphDatabase.VERTEX_FIELD_OUT_EDGES);
        }
        return set;
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }
}
