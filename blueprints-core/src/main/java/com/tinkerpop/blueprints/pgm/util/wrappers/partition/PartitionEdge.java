package com.tinkerpop.blueprints.pgm.util.wrappers.partition;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PartitionEdge extends PartitionElement implements Edge {

    public PartitionEdge(final Edge rawEdge, final PartitionGraph graph) {
        super(rawEdge, graph);
    }

    public Vertex getInVertex() {
        return new PartitionVertex(((Edge) rawElement).getInVertex(), this.graph);
    }

    public Vertex getOutVertex() {
        return new PartitionVertex(((Edge) rawElement).getOutVertex(), this.graph);
    }

    public String getLabel() {
        return ((Edge) this.rawElement).getLabel();
    }

    public Edge getRawEdge() {
        return (Edge) this.rawElement;
    }
}
