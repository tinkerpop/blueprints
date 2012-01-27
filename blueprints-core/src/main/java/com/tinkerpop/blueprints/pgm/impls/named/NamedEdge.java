package com.tinkerpop.blueprints.pgm.impls.named;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NamedEdge extends NamedElement implements Edge {

    public NamedEdge(final Edge rawEdge) {
        super(rawEdge);
    }

    public Vertex getInVertex() {
        return new NamedVertex(((Edge) rawElement).getInVertex());
    }

    public Vertex getOutVertex() {
        return new NamedVertex(((Edge) rawElement).getOutVertex());
    }

    public String getLabel() {
        return ((Edge) this.rawElement).getLabel();
    }

    public Edge getRawEdge() {
        return (Edge) this.rawElement;
    }
}
