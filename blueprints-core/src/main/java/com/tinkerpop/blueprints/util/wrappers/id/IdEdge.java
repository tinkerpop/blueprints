package com.tinkerpop.blueprints.util.wrappers.id;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdEdge extends IdElement implements Edge {

    protected IdEdge(final Edge base) {
        super(base);
    }

    public Edge getBaseEdge() {
        return (Edge) this.baseElement;
    }

    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        return new IdVertex(((Edge) baseElement).getVertex(direction));
    }

    public String getLabel() {
        return ((Edge) this.baseElement).getLabel();
    }

    public void setProperty(final String key, final Object value) {
        super.setProperty(key, value);
    }

    public String toString() {
        return "IdEdge(" + getId() + "," + baseElement + ")";
    }
}
