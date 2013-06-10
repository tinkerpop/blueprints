package com.tinkerpop.blueprints.util.wrappers.readonly;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.wrappers.WrapperVertexQuery;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyVertex extends ReadOnlyElement implements Vertex {

    public ReadOnlyVertex(final Vertex baseVertex) {
        super(baseVertex);
    }

    public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
        return new ReadOnlyEdgeIterable(((Vertex) this.baseElement).getEdges(direction, labels));
    }

    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return new ReadOnlyVertexIterable(((Vertex) this.baseElement).getVertices(direction, labels));
    }

    public Edge addEdge(final String label, final Vertex vertex) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    public VertexQuery query() {
        return new WrapperVertexQuery(((Vertex) this.baseElement).query()) {
            @Override
            public Iterable<Vertex> vertices() {
                return new ReadOnlyVertexIterable(this.query.vertices());
            }

            @Override
            public Iterable<Edge> edges() {
                return new ReadOnlyEdgeIterable(this.query.edges());
            }
        };
    }
}
