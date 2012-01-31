package com.tinkerpop.blueprints.pgm.impls.named;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.named.util.NamedEdgeSequence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class NamedVertex extends NamedElement implements Vertex {

    public NamedVertex(final Vertex rawVertex, final NamedGraph graph) {
        super(rawVertex, graph);
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        return new NamedEdgeSequence(((Vertex) this.rawElement).getOutEdges(labels).iterator(), this.graph);
    }

    public Iterable<Edge> getInEdges(final String... labels) {
        return new NamedEdgeSequence(((Vertex) this.rawElement).getInEdges(labels).iterator(), this.graph);
    }

    public Vertex getRawVertex() {
        return (Vertex) this.rawElement;
    }
}
