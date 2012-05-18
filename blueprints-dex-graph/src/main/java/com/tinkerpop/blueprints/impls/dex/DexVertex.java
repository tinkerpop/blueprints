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
        if (direction.equals(Direction.OUT)) {
            return this.getOutVertices(labels);
        } else if (direction.equals(Direction.IN))
            return this.getInVertices(labels);
        else {
            return new MultiIterable<Vertex>(Arrays.asList(this.getInVertices(labels), this.getOutVertices(labels)));
        }
    }

    private Iterable<Edge> getOutEdgesNoLabels() {
        com.sparsity.dex.gdb.TypeList tlist = graph.getRawGraph().findEdgeTypes();
        final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
        for (Integer etype : tlist) {
            edges.add(getOutEdgesSingleType(etype));
        }
        tlist = null;
        return new MultiIterable<Edge>(edges);
    }

    private Iterable<Vertex> getOutVerticesNoLabels() {
        com.sparsity.dex.gdb.TypeList tlist = graph.getRawGraph().findEdgeTypes();
        final List<Iterable<Vertex>> vertices = new ArrayList<Iterable<Vertex>>();
        for (Integer etype : tlist) {
            vertices.add(getOutVerticesSingleType(etype));
        }
        tlist = null;
        return new MultiIterable<Vertex>(vertices);
    }

    private Iterable<Edge> getInEdgesNoLabels() {
        com.sparsity.dex.gdb.TypeList tlist = graph.getRawGraph().findEdgeTypes();
        final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
        for (Integer etype : tlist) {
            edges.add(getInEdgesSingleType(etype));
        }
        tlist = null;
        return new MultiIterable<Edge>(edges);
    }

    private Iterable<Vertex> getInVerticesNoLabels() {
        com.sparsity.dex.gdb.TypeList tlist = graph.getRawGraph().findEdgeTypes();
        final List<Iterable<Vertex>> vertices = new ArrayList<Iterable<Vertex>>();
        for (Integer etype : tlist) {
            vertices.add(getInVerticesSingleType(etype));
        }
        tlist = null;
        return new MultiIterable<Vertex>(vertices);
    }

    private Iterable<Edge> getOutEdgesSingleLabel(final String label) {
        int type = DexTypes.getTypeId(graph.getRawGraph(), label);
        if (type == com.sparsity.dex.gdb.Type.InvalidType) {
            return new ArrayList<Edge>();
        }

        return getOutEdgesSingleType(type);
    }

    private Iterable<Vertex> getOutVerticesSingleLabel(final String label) {
        int type = DexTypes.getTypeId(graph.getRawGraph(), label);
        if (type == com.sparsity.dex.gdb.Type.InvalidType) {
            return new ArrayList<Vertex>();
        }

        return getOutVerticesSingleType(type);
    }

    private Iterable<Edge> getOutEdgesSingleType(final int type) {
        com.sparsity.dex.gdb.Objects objs = graph.getRawGraph().explode(oid, type, com.sparsity.dex.gdb.EdgesDirection.Outgoing);
        return new DexIterable<Edge>(graph, objs, Edge.class);
    }

    private Iterable<Vertex> getOutVerticesSingleType(final int type) {
        com.sparsity.dex.gdb.Objects objs = graph.getRawGraph().neighbors(oid, type, com.sparsity.dex.gdb.EdgesDirection.Outgoing);
        return new DexIterable<Vertex>(graph, objs, Vertex.class);
    }

    private Iterable<Edge> getInEdgesSingleLabel(final String label) {
        int type = DexTypes.getTypeId(graph.getRawGraph(), label);
        if (type == com.sparsity.dex.gdb.Type.InvalidType) {
            return new ArrayList<Edge>();
        }

        return getInEdgesSingleType(type);
    }

    private Iterable<Vertex> getInVerticesSingleLabel(final String label) {
        int type = DexTypes.getTypeId(graph.getRawGraph(), label);
        if (type == com.sparsity.dex.gdb.Type.InvalidType) {
            return new ArrayList<Vertex>();
        }

        return getInVerticesSingleType(type);
    }

    private Iterable<Edge> getInEdgesSingleType(final int type) {
        com.sparsity.dex.gdb.Objects objs = graph.getRawGraph().explode(oid, type, com.sparsity.dex.gdb.EdgesDirection.Ingoing);
        return new DexIterable<Edge>(graph, objs, Edge.class);
    }

    private Iterable<Vertex> getInVerticesSingleType(final int type) {
        com.sparsity.dex.gdb.Objects objs = graph.getRawGraph().neighbors(oid, type, com.sparsity.dex.gdb.EdgesDirection.Ingoing);
        return new DexIterable<Vertex>(graph, objs, Vertex.class);
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

    private Iterable<Vertex> getInVertices(final String... labels) {
        if (labels.length == 0)
            return this.getInVerticesNoLabels();
        else if (labels.length == 1) {
            return this.getInVerticesSingleLabel(labels[0]);
        } else {
            final List<Iterable<Vertex>> vertices = new ArrayList<Iterable<Vertex>>();
            for (final String label : labels) {
                vertices.add(this.getInVerticesSingleLabel(label));
            }
            return new MultiIterable<Vertex>(vertices);
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

    private Iterable<Vertex> getOutVertices(final String... labels) {
        if (labels.length == 0)
            return this.getOutVerticesNoLabels();
        else if (labels.length == 1) {
            return this.getOutVerticesSingleLabel(labels[0]);
        } else {
            final List<Iterable<Vertex>> vertices = new ArrayList<Iterable<Vertex>>();
            for (final String label : labels) {
                vertices.add(this.getOutVerticesSingleLabel(label));
            }
            return new MultiIterable<Vertex>(vertices);
        }
    }

    public Query query() {
        return new DefaultQuery(this);
    }
}
