package com.tinkerpop.blueprints.pgm.impls.readonly;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyEdge extends ReadOnlyElement implements Edge {

    public ReadOnlyEdge(final Edge edge) {
        super(edge);
    }

    public Vertex getOutVertex() {
        return new ReadOnlyVertex(((Edge) this.element).getOutVertex());
    }

    public Vertex getInVertex() {
        return new ReadOnlyVertex(((Edge) this.element).getInVertex());
    }

    public String getLabel() {
        return ((Edge) this.element).getLabel();
    }
}
