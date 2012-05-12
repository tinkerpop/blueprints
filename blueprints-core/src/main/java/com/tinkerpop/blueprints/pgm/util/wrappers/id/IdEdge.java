package com.tinkerpop.blueprints.pgm.util.wrappers.id;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdEdge extends IdElement implements Edge {
    private IdVertex inVertex;
    private IdVertex outVertex;

    public IdEdge(final Edge base) {
        super(base);
    }

    public Edge getBase() {
        return (Edge) this.baseElement;
    }

    public Vertex getOutVertex() {
        if (null == outVertex) {
            outVertex = new IdVertex(((Edge) this.baseElement).getOutVertex());
        }

        return outVertex;
    }

    public Vertex getInVertex() {
        if (null == inVertex) {
            inVertex = new IdVertex(((Edge) this.baseElement).getInVertex());
        }

        return inVertex;
    }

    public String getLabel() {
        return ((Edge) this.baseElement).getLabel();
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof IdEdge && ((Edge) other).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return 808068 + getId().hashCode();
    }
}
