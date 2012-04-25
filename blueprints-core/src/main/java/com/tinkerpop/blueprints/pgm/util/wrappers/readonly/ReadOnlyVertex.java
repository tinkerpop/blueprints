package com.tinkerpop.blueprints.pgm.util.wrappers.readonly;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.readonly.util.ReadOnlyEdgeSequence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyVertex extends ReadOnlyElement implements Vertex {

    public ReadOnlyVertex(final Vertex rawVertex) {
        super(rawVertex);
    }

    public Iterable<Edge> getInEdges(final Object... filters) {
        return new ReadOnlyEdgeSequence(((Vertex) this.rawElement).getInEdges(filters).iterator());
    }

    public Iterable<Edge> getOutEdges(final Object... filters) {
        return new ReadOnlyEdgeSequence(((Vertex) this.rawElement).getOutEdges(filters).iterator());
    }


}
