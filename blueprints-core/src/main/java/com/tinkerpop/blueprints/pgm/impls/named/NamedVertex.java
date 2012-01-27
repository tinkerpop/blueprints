package com.tinkerpop.blueprints.pgm.impls.named;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.wrapped.util.WrappedEdgeSequence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NamedVertex extends NamedElement implements Vertex {

    public NamedVertex(final Vertex rawVertex) {
        super(rawVertex);
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        return new WrappedEdgeSequence(((Vertex) this.rawElement).getOutEdges(labels).iterator());
    }

    public Iterable<Edge> getInEdges(final String... labels) {
        return new WrappedEdgeSequence(((Vertex) this.rawElement).getInEdges(labels).iterator());
    }

    public Vertex getRawVertex() {
        return (Vertex) this.rawElement;
    }
}
