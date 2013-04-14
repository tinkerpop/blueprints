package com.tinkerpop.blueprints.util.wrappers.readonly;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyEdge extends ReadOnlyElement implements Edge {

    public ReadOnlyEdge(final Edge baseEdge) {
        super(baseEdge);
    }

    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        return new ReadOnlyVertex(((Edge) baseElement).getVertex(direction));
    }

    public String getLabel() {
        return ((Edge) this.baseElement).getLabel();
    }
}
