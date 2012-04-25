package com.tinkerpop.blueprints.pgm.util.wrappers.wrapped;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.wrapped.util.WrappedEdgeSequence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedVertex extends WrappedElement implements Vertex {

    public WrappedVertex(final Vertex rawVertex) {
        super(rawVertex);
    }

    public Iterable<Edge> getOutEdges(final Object... filters) {
        return new WrappedEdgeSequence(((Vertex) this.rawElement).getOutEdges(filters).iterator());
    }

    public Iterable<Edge> getInEdges(final Object... filters) {
        return new WrappedEdgeSequence(((Vertex) this.rawElement).getInEdges(filters).iterator());
    }

    public Vertex getRawVertex() {
        return (Vertex) this.rawElement;
    }
}
