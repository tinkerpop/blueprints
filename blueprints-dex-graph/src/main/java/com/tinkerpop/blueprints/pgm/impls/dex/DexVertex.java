/**
 *
 */
package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Filter;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.FilteredEdgeIterable;
import com.tinkerpop.blueprints.pgm.impls.MultiIterable;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.dex.util.DexTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Vertex} implementation for Dex.
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class DexVertex extends DexElement implements Vertex {
    /**
     * Creates a new instance.
     *
     * @param g   DexGraph.
     * @param oid Dex OID.
     */
    protected DexVertex(final DexGraph g, final long oid) {
        super(g, oid);
    }

    private Iterable<Edge> getOutEdgesNoLabels() {
        com.sparsity.dex.gdb.Objects result = graph.getRawSession().newObjects();
        com.sparsity.dex.gdb.TypeList tlist = graph.getRawGraph().findEdgeTypes();
        for (Integer etype : tlist) {
            com.sparsity.dex.gdb.Objects objs = graph.getRawGraph().explode(oid, etype, com.sparsity.dex.gdb.EdgesDirection.Outgoing);
            result.union(objs);
            objs.close();
        }
        tlist = null;
        return new DexIterable<Edge>(graph, result, Edge.class);
    }

    private Iterable<Edge> getInEdgesNoLabels() {
        com.sparsity.dex.gdb.Objects result = graph.getRawSession().newObjects();
        com.sparsity.dex.gdb.TypeList tlist = graph.getRawGraph().findEdgeTypes();
        for (Integer etype : tlist) {
            com.sparsity.dex.gdb.Objects objs = graph.getRawGraph().explode(oid, etype, com.sparsity.dex.gdb.EdgesDirection.Ingoing);
            result.union(objs);
            objs.close();
        }
        tlist = null;
        return new DexIterable<Edge>(graph, result, Edge.class);
    }

    private Iterable<Edge> getOutEdgesSingleLabel(final String label) {
        int type = DexTypes.getTypeId(graph.getRawGraph(), label);
        if (type == com.sparsity.dex.gdb.Type.InvalidType) {
            return new ArrayList<Edge>();
        }

        com.sparsity.dex.gdb.Objects objs = graph.getRawGraph().explode(oid, type, com.sparsity.dex.gdb.EdgesDirection.Outgoing);
        return new DexIterable<Edge>(graph, objs, Edge.class);
    }

    private Iterable<Edge> getInEdgesSingleLabel(final String label) {
        int type = DexTypes.getTypeId(graph.getRawGraph(), label);
        if (type == com.sparsity.dex.gdb.Type.InvalidType) {
            return new ArrayList<Edge>();
        }

        com.sparsity.dex.gdb.Objects objs = graph.getRawGraph().explode(oid, type, com.sparsity.dex.gdb.EdgesDirection.Ingoing);
        return new DexIterable<Edge>(graph, objs, Edge.class);
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    public Iterable<Edge> getInEdges(final Object... filters) {
        if (filters.length == 0)
            return this.getInEdgesNoLabels();
        else if (filters.length == 1) {
            if (filters[0] instanceof String)
                return this.getInEdgesSingleLabel((String) filters[0]);
            else if (filters[0] instanceof Filter)
                return new FilteredEdgeIterable(this.getInEdgesNoLabels(), FilteredEdgeIterable.getFilter(filters));
            else
                throw new IllegalArgumentException(Vertex.TYPE_ERROR_MESSAGE);
        } else {
            final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            int counter = 0;
            for (final Object filter : filters) {
                if (filter instanceof String) {
                    counter++;
                    edges.add(this.getInEdgesSingleLabel((String) filter));
                }
            }

            if (edges.size() == filters.length)
                return new MultiIterable<Edge>(edges);
            else if (counter == 0)
                return new FilteredEdgeIterable(this.getInEdgesNoLabels(), FilteredEdgeIterable.getFilter(filters));
            else
                return new FilteredEdgeIterable(new MultiIterable<Edge>(edges), FilteredEdgeIterable.getFilter(filters));
        }
    }

    public Iterable<Edge> getOutEdges(final Object... filters) {
        if (filters.length == 0)
            return this.getOutEdgesNoLabels();
        else if (filters.length == 1) {
            if (filters[0] instanceof String)
                return this.getOutEdgesSingleLabel((String) filters[0]);
            else if (filters[0] instanceof Filter)
                return new FilteredEdgeIterable(this.getOutEdgesNoLabels(), FilteredEdgeIterable.getFilter(filters));
            else
                throw new IllegalArgumentException(Vertex.TYPE_ERROR_MESSAGE);
        } else {
            final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            int counter = 0;
            for (final Object filter : filters) {
                if (filter instanceof String) {
                    counter++;
                    edges.add(this.getOutEdgesSingleLabel((String) filter));
                }
            }

            if (edges.size() == filters.length)
                return new MultiIterable<Edge>(edges);
            else if (counter == 0)
                return new FilteredEdgeIterable(this.getOutEdgesNoLabels(), FilteredEdgeIterable.getFilter(filters));
            else
                return new FilteredEdgeIterable(new MultiIterable<Edge>(edges), FilteredEdgeIterable.getFilter(filters));
        }
    }
}
