package com.tinkerpop.blueprints.util.wrappers.partition;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionEdge extends PartitionElement implements Edge {

    protected PartitionEdge(final Edge baseEdge, final PartitionGraph graph) {
        super(baseEdge, graph);
    }

    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        final Vertex vertex = ((Edge) baseElement).getVertex(direction);
        return graph.isInPartition(vertex) ? new PartitionVertex(((Edge) baseElement).getVertex(direction), graph) : null;
    }

    public String getLabel() {
        return ((Edge) this.baseElement).getLabel();
    }

    public Edge getBaseEdge() {
        return (Edge) this.baseElement;
    }
}
