package com.tinkerpop.blueprints.pgm.util.wrappers.wrapped;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Query;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.WrapperQuery;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WrappedVertex extends WrappedElement implements Vertex {

    public WrappedVertex(final Vertex baseVertex) {
        super(baseVertex);
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        return new WrappedEdgeIterable(((Vertex) this.baseElement).getOutEdges(labels));
    }

    public Iterable<Edge> getInEdges(final String... labels) {
        return new WrappedEdgeIterable(((Vertex) this.baseElement).getInEdges(labels));
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
