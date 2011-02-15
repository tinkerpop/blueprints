package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.db.graph.OGraphVertex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

import java.util.Set;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientVertex extends OrientElement implements Vertex {

    public OrientVertex(final OrientGraph iGraph, final OGraphVertex rawVertex) {
        super(iGraph, rawVertex);
    }

    public Iterable<Edge> getOutEdges() {
        return new OrientElementSequence<Edge>(graph, ((OGraphVertex) this.rawElement).getOutEdges().iterator());
    }

    public Iterable<Edge> getInEdges() {
        return new OrientElementSequence<Edge>(graph, ((OGraphVertex) this.rawElement).getInEdges().iterator());
    }

    public Iterable<Edge> getOutEdges(final String label) {
        return new OrientEdgeSequence(new OrientElementSequence<Edge>(graph, ((OGraphVertex) this.rawElement).getOutEdges().iterator()), label);
        //return new OrientElementSequence<Edge>(graph, ((OGraphVertex) this.rawElement).getOutEdges(label).iterator());
    }

    public Iterable<Edge> getInEdges(final String label) {
        return new OrientEdgeSequence(new OrientElementSequence<Edge>(graph, ((OGraphVertex) this.rawElement).getInEdges().iterator()), label);
        //return new OrientElementSequence<Edge>(graph, ((OGraphVertex) this.rawElement).getInEdges(label).iterator());
    }

    public Set<String> getPropertyKeys() {
        final Set<String> set = super.getPropertyKeys();
        if (set.size() > 0) {
            set.remove(OGraphDatabase.VERTEX_FIELD_IN_EDGES);
            set.remove(OGraphDatabase.VERTEX_FIELD_OUT_EDGES);
        }
        return set;
    }

    public OGraphVertex getRawVertex() {
        return (OGraphVertex) this.rawElement;
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }
}
