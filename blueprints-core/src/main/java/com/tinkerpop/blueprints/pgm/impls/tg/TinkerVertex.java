package com.tinkerpop.blueprints.pgm.impls.tg;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.MultiIterable;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.tg.util.TinkerEdgeSequence;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerVertex extends TinkerElement implements Vertex, Serializable {

    protected Set<Edge> outEdges = new HashSet<Edge>();
    protected Set<Edge> inEdges = new HashSet<Edge>();

    protected TinkerVertex(final String id, final TinkerGraph graph) {
        super(id, graph);
    }

    public TinkerVertex getRawVertex() {
        return this;
    }

    public Iterable<Edge> getInEdges(final String... labels) {
        if (labels.length == 0)
            return new LinkedList<Edge>(this.inEdges);
        else if (labels.length == 1) {
            return new TinkerEdgeSequence(new LinkedList<Edge>(this.inEdges), labels[0]);
        } else {
            final List<Iterable<Edge>> edges = new LinkedList<Iterable<Edge>>();
            for (final String label : labels) {
                edges.add(this.getInEdges(label));
            }
            return new MultiIterable<Edge>(edges);
        }
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        if (labels.length == 0)
            return new LinkedList<Edge>(this.outEdges);
        else if (labels.length == 1) {
            return new TinkerEdgeSequence(new LinkedList<Edge>(this.outEdges), labels[0]);
        } else {
            final List<Iterable<Edge>> edges = new LinkedList<Iterable<Edge>>();
            for (final String label : labels) {
                edges.add(this.getOutEdges(label));
            }
            return new MultiIterable<Edge>(edges);
        }
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    public boolean equals(final Object object) {
        return object instanceof TinkerVertex && ((TinkerVertex) object).getId().equals(this.getId());
    }
}
