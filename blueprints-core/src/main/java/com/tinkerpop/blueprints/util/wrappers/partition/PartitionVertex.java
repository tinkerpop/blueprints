package com.tinkerpop.blueprints.util.wrappers.partition;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.WrapperQuery;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionVertex extends PartitionElement implements Vertex {

    protected PartitionVertex(final Vertex baseVertex, final PartitionGraph graph) {
        super(baseVertex, graph);
    }

    public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
        return new PartitionEdgeIterable(((Vertex) this.baseElement).getEdges(direction, labels), this.graph);
    }

    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return new PartitionVertexIterable(((Vertex) this.baseElement).getVertices(direction, labels), this.graph);
    }

    public Query query() {
        return new WrapperQuery(((Vertex) this.baseElement).query()) {
            @Override
            public Iterable<Vertex> vertices() {
                return new PartitionVertexIterable(this.query.vertices(), graph);
            }

            @Override
            public Iterable<Edge> edges() {
                return new PartitionEdgeIterable(this.query.edges(), graph);
            }
        };
    }

    public Vertex getBaseVertex() {
        return (Vertex) this.baseElement;
    }
}
