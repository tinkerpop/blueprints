package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;

import java.util.Set;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientEdge extends OrientElement implements Edge {

    public OrientEdge(final OrientGraph graph, final ODocument rawEdge) {
        super(graph, rawEdge);
    }

    public Vertex getOutVertex() {
        return new OrientVertex(graph, graph.getRawGraph().getOutVertex(rawElement));
    }

    public Vertex getInVertex() {
        return new OrientVertex(graph, graph.getRawGraph().getInVertex(rawElement));
    }

    public String getLabel() {
        return (String) this.rawElement.field(LABEL);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
		protected void setLabel(final String label) {
        this.rawElement.field(LABEL, label);
        for (OrientAutomaticIndex autoIndex : this.graph.getAutoIndices()) {
            autoIndex.autoUpdate(AutomaticIndex.LABEL, this.getLabel(), null, this);
        }
    }

    public Set<String> getPropertyKeys() {
        final Set<String> set = super.getPropertyKeys();
        set.remove(OGraphDatabase.EDGE_FIELD_IN);
        set.remove(OGraphDatabase.EDGE_FIELD_OUT);
        return set;
    }

    public String toString() {
        return StringFactory.edgeString(this);
    }

}
