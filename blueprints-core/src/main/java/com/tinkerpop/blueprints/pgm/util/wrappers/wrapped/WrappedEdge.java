package com.tinkerpop.blueprints.pgm.util.wrappers.wrapped;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedEdge extends WrappedElement implements Edge {

    public WrappedEdge(final Edge baseEdge) {
        super(baseEdge);
    }

    public Vertex getInVertex() {
        return new WrappedVertex(((Edge) baseElement).getInVertex());
    }

    public Vertex getOutVertex() {
        return new WrappedVertex(((Edge) baseElement).getOutVertex());
    }

    public String getLabel() {
        return ((Edge) this.baseElement).getLabel();
    }

    public Edge getBaseEdge() {
        return (Edge) this.baseElement;
    }
}
