package com.tinkerpop.blueprints.pgm.util.wrappers.partition;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Query;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.DefaultQuery;
import com.tinkerpop.blueprints.pgm.util.wrappers.partition.util.PartitionEdgeSequence;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionVertex extends PartitionElement implements Vertex {

    public PartitionVertex(final Vertex rawVertex, final PartitionGraph graph) {
        super(rawVertex, graph);
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        return new PartitionEdgeSequence(((Vertex) this.rawElement).getOutEdges(labels).iterator(), this.graph);
    }

    public Iterable<Edge> getInEdges(final String... labels) {
        return new PartitionEdgeSequence(((Vertex) this.rawElement).getInEdges(labels).iterator(), this.graph);
    }

    public Query query() {
        return new DefaultQuery(this);
    }

    public Vertex getBaseVertex() {
        return (Vertex) this.rawElement;
    }
}
