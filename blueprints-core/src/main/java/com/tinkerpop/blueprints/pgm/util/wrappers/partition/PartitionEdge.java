package com.tinkerpop.blueprints.pgm.util.wrappers.partition;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionEdge extends PartitionElement implements Edge {

    public PartitionEdge(final Edge baseEdge, final PartitionGraph graph) {
        super(baseEdge, graph);
    }

    public Vertex getInVertex() {
        return new PartitionVertex(((Edge) baseElement).getInVertex(), this.graph);
    }

    public Vertex getOutVertex() {
        return new PartitionVertex(((Edge) baseElement).getOutVertex(), this.graph);
    }

    public String getLabel() {
        return ((Edge) this.baseElement).getLabel();
    }

    public Edge getBaseEdge() {
        return (Edge) this.baseElement;
    }
}
