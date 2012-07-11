package com.tinkerpop.blueprints.util.wrappers.wrapped;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.WrapperQuery;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedVertex extends WrappedElement implements Vertex {

    protected WrappedVertex(final Vertex baseVertex) {
        super(baseVertex);
    }

    public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
        return new WrappedEdgeIterable(((Vertex) this.baseElement).getEdges(direction, labels));
    }

    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return new WrappedVertexIterable(((Vertex) this.baseElement).getVertices(direction, labels));
    }

    public Query query() {
        return new WrapperQuery(((Vertex) this.baseElement).query()) {
            @Override
            public Iterable<Vertex> vertices() {
                return new WrappedVertexIterable(this.query.vertices());
            }

            @Override
            public Iterable<Edge> edges() {
                return new WrappedEdgeIterable(this.query.edges());
            }
        };
    }

    public Vertex getBaseVertex() {
        return (Vertex) this.baseElement;
    }
}
