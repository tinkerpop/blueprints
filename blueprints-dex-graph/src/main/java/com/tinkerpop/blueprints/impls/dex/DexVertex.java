/**
 *
 */
package com.tinkerpop.blueprints.impls.dex;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultQuery;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.VerticesFromEdgesIterable;

import java.util.ArrayList;
import java.util.Arrays;
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

    public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
        if (direction.equals(Direction.OUT)) {
            return this.getOutEdges(labels);
        } else if (direction.equals(Direction.IN))
            return this.getInEdges(labels);
        else {
            return new MultiIterable<Edge>(Arrays.asList(this.getInEdges(labels), this.getOutEdges(labels)));
        }
    }

    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return new VerticesFromEdgesIterable(this, direction, labels);
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

    private Iterable<Edge> getInEdges(final String... labels) {
        if (labels.length == 0)
            return this.getInEdgesNoLabels();
        else if (labels.length == 1) {
            return this.getInEdgesSingleLabel(labels[0]);
        } else {
            final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            for (final String label : labels) {
                edges.add(this.getInEdgesSingleLabel(label));
            }
            return new MultiIterable<Edge>(edges);
        }
    }

    private Iterable<Edge> getOutEdges(final String... labels) {
        if (labels.length == 0)
            return this.getOutEdgesNoLabels();
        else if (labels.length == 1) {
            return this.getOutEdgesSingleLabel(labels[0]);
        } else {
            final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            for (final String label : labels) {
                edges.add(this.getOutEdgesSingleLabel(label));
            }
            return new MultiIterable<Edge>(edges);
        }
    }

    public Query query() {
        return new DefaultQuery(this);
    }
}
