package com.tinkerpop.blueprints.util.wrappers.readonly;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.WrapperQuery;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ReadOnlyVertex extends ReadOnlyElement implements Vertex {

    public ReadOnlyVertex(final Vertex baseVertex) {
        super(baseVertex);
    }

    public Iterable<Edge> getInEdges(final String... labels) {
        return new ReadOnlyEdgeIterable(((Vertex) this.baseElement).getInEdges(labels));
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        return new ReadOnlyEdgeIterable(((Vertex) this.baseElement).getOutEdges(labels));
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
