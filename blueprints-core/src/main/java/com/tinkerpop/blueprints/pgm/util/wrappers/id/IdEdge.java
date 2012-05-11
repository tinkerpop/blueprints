package com.tinkerpop.blueprints.pgm.util.wrappers.id;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
class IdEdge extends IdElement implements Edge {
    private IdVertex inVertex;
    private IdVertex outVertex;

    public IdEdge(Edge base) {
        super(base);
    }

    public Edge getBase() {
        return (Edge) base;
    }

    public Vertex getOutVertex() {
        if (null == outVertex) {
            outVertex = new IdVertex(((Edge) base).getOutVertex());
        }

        return outVertex;
    }

    public Vertex getInVertex() {
        if (null == inVertex) {
            inVertex = new IdVertex(((Edge) base).getInVertex());
        }

        return inVertex;
    }

    public String getLabel() {
        return ((Edge) base).getLabel();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof IdEdge
                && ((Edge) other).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return 808068 + getId().hashCode();
    }
}
