package com.tinkerpop.blueprints.pgm.util.wrappers.partition;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Query;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.wrappers.WrapperQuery;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionVertex extends PartitionElement implements Vertex {

    public PartitionVertex(final Vertex baseVertex, final PartitionGraph graph) {
        super(baseVertex, graph);
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        return new PartitionEdgeIterable(((Vertex) this.baseElement).getOutEdges(labels), this.graph);
    }

    public Iterable<Edge> getInEdges(final String... labels) {
        return new PartitionEdgeIterable(((Vertex) this.baseElement).getInEdges(labels), this.graph);
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
