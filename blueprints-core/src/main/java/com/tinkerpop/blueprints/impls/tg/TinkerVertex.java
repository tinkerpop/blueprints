package com.tinkerpop.blueprints.impls.tg;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultQuery;
import com.tinkerpop.blueprints.util.StringFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TinkerVertex extends TinkerElement implements Vertex, Serializable {

    protected Map<String, Set<Edge>> outEdges = new HashMap<String, Set<Edge>>();
    protected Map<String, Set<Edge>> inEdges = new HashMap<String, Set<Edge>>();

    protected TinkerVertex(final String id, final TinkerGraph graph) {
        super(id, graph);
    }

    public Iterable<Edge> getInEdges(final String... labels) {
        if (labels.length == 0) {
            final List<Edge> totalEdges = new ArrayList<Edge>();
            for (final Collection<Edge> edges : this.inEdges.values()) {
                totalEdges.addAll(edges);
            }
            return totalEdges;
        } else if (labels.length == 1) {
            final Set<Edge> edges = this.inEdges.get(labels[0]);
            if (null == edges) {
                return Collections.emptyList();
            } else {
                return new ArrayList<Edge>(edges);
            }
        } else {
            final List<Edge> totalEdges = new ArrayList<Edge>();
            for (final String label : labels) {
                final Set<Edge> edges = this.inEdges.get(label);
                if (null != edges) {
                    totalEdges.addAll(edges);
                }
            }
            return totalEdges;
        }
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        if (labels.length == 0) {
            final List<Edge> totalEdges = new ArrayList<Edge>();
            for (final Collection<Edge> edges : this.outEdges.values()) {
                totalEdges.addAll(edges);
            }
            return totalEdges;
        } else if (labels.length == 1) {
            final Set<Edge> edges = this.outEdges.get(labels[0]);
            if (null == edges) {
                return Collections.emptyList();
            } else {
                return new ArrayList<Edge>(edges);
            }
        } else {
            final List<Edge> totalEdges = new ArrayList<Edge>();
            for (final String label : labels) {
                final Set<Edge> edges = this.outEdges.get(label);
                if (null != edges) {
                    totalEdges.addAll(edges);
                }
            }
            return totalEdges;
        }
    }

    public Query query() {
        return new DefaultQuery(this);
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    public boolean equals(final Object object) {
        return object instanceof TinkerVertex && ((TinkerVertex) object).getId().equals(this.getId());
    }

    protected void addOutEdge(final String label, final Edge edge) {
        Set<Edge> edges = this.outEdges.get(label);
        if (null == edges) {
            edges = new HashSet<Edge>();
            this.outEdges.put(label, edges);
        }
        edges.add(edge);
    }

    protected void addInEdge(final String label, final Edge edge) {
        Set<Edge> edges = this.inEdges.get(label);
        if (null == edges) {
            edges = new HashSet<Edge>();
            this.inEdges.put(label, edges);
        }
        edges.add(edge);
    }
}
