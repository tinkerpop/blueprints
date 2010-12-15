package com.tinkerpop.blueprints.pgm.impls.orientdb;

import com.orientechnologies.orient.core.db.graph.OGraphEdge;
import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;

import java.util.Set;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientEdge extends OrientElement implements Edge {

    public OrientEdge(final OrientGraph graph, final OGraphEdge rawEdge) {
        super(graph, rawEdge);
    }

    public Vertex getOutVertex() {
        return new OrientVertex(graph, getRawEdge().getOut());
    }

    public Vertex getInVertex() {
        return new OrientVertex(graph, getRawEdge().getIn());
    }

    public OGraphEdge getRawEdge() {
        return (OGraphEdge) this.rawElement;
    }

    public String getLabel() {
        return (String) this.rawElement.get(LABEL);
    }

    protected void setLabel(final String label) {
        this.rawElement.set(LABEL, label);
        for (OrientAutomaticIndex autoIndex : this.graph.getAutoIndices()) {
            autoIndex.autoUpdate(AutomaticIndex.LABEL, this.getLabel(), null, this);
        }
    }

    public Set<String> getPropertyKeys() {
        final Set<String> set = super.getPropertyKeys();
        set.remove(OGraphEdge.IN);
        set.remove(OGraphEdge.OUT);
        return set;
    }

    public String toString() {
        return StringFactory.edgeString(this);
    }

}
