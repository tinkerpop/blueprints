package com.tinkerpop.blueprints.util.wrappers.readonly;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyEdge extends ReadOnlyElement implements Edge {

    public ReadOnlyEdge(final Edge baseEdge) {
        super(baseEdge);
    }

    public Vertex getOutVertex() {
        return new ReadOnlyVertex(((Edge) this.baseElement).getOutVertex());
    }

    public Vertex getInVertex() {
        return new ReadOnlyVertex(((Edge) this.baseElement).getInVertex());
    }

    public String getLabel() {
        return ((Edge) this.baseElement).getLabel();
    }
}
