package com.tinkerpop.blueprints.pgm.impls.tg;

import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;

import java.io.Serializable;


/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerEdge extends TinkerElement implements Edge, Serializable {

    private final String label;
    private final Vertex inVertex;
    private final Vertex outVertex;

    protected TinkerEdge(final String id, final Vertex outVertex, final Vertex inVertex, final String label, final TinkerGraph graph) {
        super(id, graph);
        this.label = label;
        this.outVertex = outVertex;
        this.inVertex = inVertex;
        for (TinkerAutomaticIndex index : this.graph.getAutoIndices()) {
            index.autoUpdate(AutomaticIndex.LABEL, this.label, null, this);
        }
    }

    public TinkerEdge getRawEdge() {
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public Vertex getOutVertex() {
        return this.outVertex;
    }

    public Vertex getInVertex() {
        return this.inVertex;
    }

    public String toString() {
        return StringFactory.edgeString(this);
    }

    public boolean equals(final Object object) {
        return object instanceof TinkerEdge && ((TinkerEdge) object).getId().equals(this.getId());
    }

}
