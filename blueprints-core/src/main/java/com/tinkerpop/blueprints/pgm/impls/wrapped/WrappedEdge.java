package com.tinkerpop.blueprints.pgm.impls.wrapped;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedEdge extends WrappedElement implements Edge {

    public WrappedEdge(final Edge rawEdge) {
        super(rawEdge);
    }

    public Vertex getInVertex() {
        return new WrappedVertex(((Edge) rawElement).getInVertex());
    }

    public Vertex getOutVertex() {
        return new WrappedVertex(((Edge) rawElement).getOutVertex());
    }

    public String getLabel() {
        return ((Edge) this.rawElement).getLabel();
    }

    public Edge getRawEdge() {
        return (Edge) this.rawElement;
    }
}
