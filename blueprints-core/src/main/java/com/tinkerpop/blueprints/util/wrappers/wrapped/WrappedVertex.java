package com.tinkerpop.blueprints.util.wrappers.wrapped;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.wrappers.WrapperVertexQuery;

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

    public VertexQuery query() {
        return new WrapperVertexQuery(((Vertex) this.baseElement).query()) {
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

    public Edge addEdge(final String label, final Vertex vertex) {
        if (vertex instanceof WrappedVertex)
            return new WrappedEdge(((Vertex) this.baseElement).addEdge(label, ((WrappedVertex) vertex).getBaseVertex()));
        else
            return new WrappedEdge(((Vertex) this.baseElement).addEdge(label, vertex));
    }

    public Vertex getBaseVertex() {
        return (Vertex) this.baseElement;
    }
}
