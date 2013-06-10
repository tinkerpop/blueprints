package com.tinkerpop.blueprints.util.wrappers.id;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdEdge extends IdElement implements Edge {

    protected IdEdge(final Edge base, final IdGraph idGraph) {
        super(base, idGraph, idGraph.getSupportEdgeIds());
    }

    public Edge getBaseEdge() {
        return (Edge) this.baseElement;
    }

    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        return new IdVertex(((Edge) baseElement).getVertex(direction), this.idGraph);
    }

    public String getLabel() {
        return ((Edge) this.baseElement).getLabel();
    }

    public void setProperty(final String key, final Object value) {
        super.setProperty(key, value);
    }

    public String toString() {
        return StringFactory.edgeString(this);
    }
}
