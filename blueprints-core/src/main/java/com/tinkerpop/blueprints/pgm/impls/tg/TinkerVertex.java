package com.tinkerpop.blueprints.pgm.impls.tg;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Filter;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.FilteredEdgeIterable;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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

    public TinkerVertex getRawVertex() {
        return this;
    }

    private static List<Edge> getAllEdges(final Map<String, Set<Edge>> theEdges) {
        final List<Edge> totalEdges = new LinkedList<Edge>();
        for (final Collection<Edge> edges : theEdges.values()) {
            totalEdges.addAll(edges);
        }
        return totalEdges;
    }

    public Iterable<Edge> getInEdges(final Object... filters) {
        if (filters.length == 0) {
            return getAllEdges(this.inEdges);
        } else if (filters.length == 1) {
            if (filters[0] instanceof String) {
                final Set<Edge> edges = this.inEdges.get(filters[0]);
                if (null == edges) {
                    return Collections.emptyList();
                } else {
                    return new LinkedList<Edge>(edges);
                }
            } else if (filters[0] instanceof Filter) {
                return new FilteredEdgeIterable(getAllEdges(this.inEdges), (Filter) filters[0]);
            } else {
                throw new IllegalArgumentException(Vertex.TYPE_ERROR_MESSAGE);
            }
        } else {
            final Filter filter = FilteredEdgeIterable.getFilter(filters);
            final List<String> labels = FilteredEdgeIterable.getLabels(filters);

            List<Edge> totalEdges = new LinkedList<Edge>();
            for (final String label : labels) {
                final Set<Edge> edges = this.inEdges.get(label);
                if (null != edges) {
                    totalEdges.addAll(edges);
                }
            }

            if (labels.size() == filters.length)
                return totalEdges;

            if (labels.isEmpty()) {
                totalEdges = getAllEdges(this.inEdges);
            }

            if (null == filter)
                return totalEdges;
            else
                return new FilteredEdgeIterable(totalEdges, filter);
        }
    }

    public Iterable<Edge> getOutEdges(final Object... filters) {
        if (filters.length == 0) {
            return getAllEdges(this.outEdges);
        } else if (filters.length == 1) {
            if (filters[0] instanceof String) {
                final Set<Edge> edges = this.outEdges.get(filters[0]);
                if (null == edges) {
                    return Collections.emptyList();
                } else {
                    return new LinkedList<Edge>(edges);
                }
            } else if (filters[0] instanceof Filter) {
                return new FilteredEdgeIterable(getAllEdges(this.outEdges), (Filter) filters[0]);
            } else {
                throw new IllegalArgumentException(Vertex.TYPE_ERROR_MESSAGE);
            }
        } else {
            final Filter filter = FilteredEdgeIterable.getFilter(filters);
            final List<String> labels = FilteredEdgeIterable.getLabels(filters);

            List<Edge> totalEdges = new LinkedList<Edge>();
            for (final String label : labels) {
                final Set<Edge> edges = this.outEdges.get(label);
                if (null != edges) {
                    totalEdges.addAll(edges);
                }
            }

            if (labels.size() == filters.length)
                return totalEdges;

            if (labels.isEmpty()) {
                totalEdges = getAllEdges(this.outEdges);
            }

            if (null == filter)
                return totalEdges;
            else
                return new FilteredEdgeIterable(totalEdges, filter);
        }
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
