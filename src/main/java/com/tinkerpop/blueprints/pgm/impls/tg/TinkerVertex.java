package com.tinkerpop.blueprints.pgm.impls.tg;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerVertex extends TinkerElement implements Vertex {

    protected Set<Edge> outEdges = new HashSet<Edge>();
    protected Set<Edge> inEdges = new HashSet<Edge>();

    protected TinkerVertex(final String id, final TinkerGraph graph) {
        super(id, graph);
    }

    public TinkerVertex getRawVertex() {
        return this;
    }

    public Iterable<Edge> getOutEdges() {
        return this.outEdges;
    }

    public Iterable<Edge> getInEdges() {
        return this.inEdges;
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    public boolean equals(final Object object) {
        return object instanceof TinkerVertex && ((TinkerVertex) object).getId().equals(this.getId());
    }
}
