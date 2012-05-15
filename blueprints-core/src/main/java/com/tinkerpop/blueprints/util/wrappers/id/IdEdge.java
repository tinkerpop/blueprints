package com.tinkerpop.blueprints.util.wrappers.id;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdEdge extends IdElement implements Edge {

    public IdEdge(final Edge base) {
        super(base);
    }

    public Edge getBase() {
        return (Edge) this.baseElement;
    }

    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        return new IdVertex(((Edge) baseElement).getVertex(direction));
    }

    public String getLabel() {
        return ((Edge) this.baseElement).getLabel();
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof IdEdge && ((Edge) other).getId().equals(getId());
    }
}
