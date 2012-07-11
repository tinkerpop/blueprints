package com.tinkerpop.blueprints.util.wrappers.readonly;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.WrapperQuery;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class ReadOnlyVertex extends ReadOnlyElement implements Vertex {

    protected ReadOnlyVertex(final Vertex baseVertex) {
        super(baseVertex);
    }

    public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
        return new ReadOnlyEdgeIterable(((Vertex) this.baseElement).getEdges(direction, labels));
    }

    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return new ReadOnlyVertexIterable(((Vertex) this.baseElement).getVertices(direction, labels));
    }

    public Query query() {
        return new WrapperQuery(((Vertex) this.baseElement).query()) {
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
