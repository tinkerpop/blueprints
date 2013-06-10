package com.tinkerpop.blueprints.util.wrappers.partition;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.wrappers.WrapperVertexQuery;

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

    public VertexQuery query() {
        return new WrapperVertexQuery(((Vertex) this.baseElement).query()) {
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

    public Edge addEdge(final String label, final Vertex vertex) {
        return this.graph.addEdge(null, this, vertex, label);
    }

    public Vertex getBaseVertex() {
        return (Vertex) this.baseElement;
    }
}
