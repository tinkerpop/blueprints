package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphVertex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.orientdb.util.OrientElementSequence;

import java.util.Set;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientVertex extends OrientElement implements Vertex {

    public OrientVertex(final OrientGraph graph) {
        super(graph, new OGraphVertex(graph.getRawGraph()));
        this.rawElement.save();
    }

    public OrientVertex(final OrientGraph graph, final OGraphVertex rawVertex) {
        super(graph, rawVertex);
    }

    public Iterable<Edge> getOutEdges() {
        return new OrientElementSequence<Edge>(this.graph, ((OGraphVertex) this.rawElement).getOutEdges().iterator());
    }

    public Iterable<Edge> getInEdges() {
        return new OrientElementSequence<Edge>(this.graph, ((OGraphVertex) this.rawElement).getInEdges().iterator());
    }

    public Set<String> getPropertyKeys() {
        final Set<String> set = super.getPropertyKeys();
        set.remove(OGraphVertex.FIELD_IN_EDGES);
        set.remove(OGraphVertex.FIELD_OUT_EDGES);
        return set;
    }

    public OGraphVertex getRawVertex() {
        return (OGraphVertex) this.rawElement;
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

}
