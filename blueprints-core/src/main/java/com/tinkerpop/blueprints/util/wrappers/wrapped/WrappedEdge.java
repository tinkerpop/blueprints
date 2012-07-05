package com.tinkerpop.blueprints.util.wrappers.wrapped;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedEdge extends WrappedElement implements Edge {

    protected WrappedEdge(final Edge baseEdge) {
        super(baseEdge);
    }

    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        return new WrappedVertex(((Edge) baseElement).getVertex(direction));
    }

    public String getLabel() {
        return ((Edge) this.baseElement).getLabel();
    }

    public Edge getBaseEdge() {
        return (Edge) this.baseElement;
    }

    public Edge setProperty(final String key, final Object value) {
        return (Edge) super.setProperty(key, value);
    }
}
