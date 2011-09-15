package com.tinkerpop.blueprints.pgm.impls.readonly;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.readonly.util.ReadOnlyEdgeSequence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyVertex extends ReadOnlyElement implements Vertex {

    public ReadOnlyVertex(final Vertex rawVertex) {
        super(rawVertex);
    }

    public Iterable<Edge> getInEdges(final String... labels) {
        return new ReadOnlyEdgeSequence(((Vertex) this.rawElement).getInEdges(labels).iterator());
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        return new ReadOnlyEdgeSequence(((Vertex) this.rawElement).getOutEdges(labels).iterator());
    }


}
