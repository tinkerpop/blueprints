package com.tinkerpop.blueprints.util.wrappers.readonly;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class ReadOnlyEdge extends ReadOnlyElement implements Edge {

    protected ReadOnlyEdge(final Edge baseEdge) {
        super(baseEdge);
    }

    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        return new ReadOnlyVertex(((Edge) baseElement).getVertex(direction));
    }

    public String getLabel() {
        return ((Edge) this.baseElement).getLabel();
    }

    public Edge setProperty(final String key, final Object value) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }
}
