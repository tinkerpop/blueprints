package com.tinkerpop.blueprints.pgm.impls.readonly;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.readonly.util.ReadOnlyEdgeSequence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyVertex extends ReadOnlyElement implements Vertex {

    public ReadOnlyVertex(final Vertex vertex) {
        super(vertex);
    }

    public Iterable<Edge> getInEdges() {
        return new ReadOnlyEdgeSequence(((Vertex) this.element).getInEdges().iterator());
    }

    public Iterable<Edge> getOutEdges() {
        return new ReadOnlyEdgeSequence(((Vertex) this.element).getOutEdges().iterator());
    }

    public Iterable<Edge> getInEdges(final String label) {
        return new ReadOnlyEdgeSequence(((Vertex) this.element).getInEdges(label).iterator());
    }

    public Iterable<Edge> getOutEdges(final String label) {
        return new ReadOnlyEdgeSequence(((Vertex) this.element).getOutEdges(label).iterator());
    }


}
