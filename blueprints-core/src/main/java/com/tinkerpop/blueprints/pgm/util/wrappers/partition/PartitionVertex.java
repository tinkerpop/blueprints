package com.tinkerpop.blueprints.pgm.util.wrappers.partition;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Query;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.DefaultQuery;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionVertex extends PartitionElement implements Vertex {

    public PartitionVertex(final Vertex rawVertex, final PartitionGraph graph) {
        super(rawVertex, graph);
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        return new PartitionEdgeIterable(((Vertex) this.rawElement).getOutEdges(labels), this.graph);
    }

    public Iterable<Edge> getInEdges(final String... labels) {
        return new PartitionEdgeIterable(((Vertex) this.rawElement).getInEdges(labels), this.graph);
    }

    public Query query() {
        return new DefaultQuery(this);
    }

    public Vertex getBaseVertex() {
        return (Vertex) this.rawElement;
    }
}
