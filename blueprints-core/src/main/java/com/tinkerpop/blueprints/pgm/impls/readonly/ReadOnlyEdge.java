package com.tinkerpop.blueprints.pgm.impls.readonly;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyEdge extends ReadOnlyElement implements Edge {

    public ReadOnlyEdge(final Edge rawEdge) {
        super(rawEdge);
    }

    public Vertex getOutVertex() {
        return new ReadOnlyVertex(((Edge) this.rawElement).getOutVertex());
    }

    public Vertex getInVertex() {
        return new ReadOnlyVertex(((Edge) this.rawElement).getInVertex());
    }

    public String getLabel() {
        return ((Edge) this.rawElement).getLabel();
    }
}
