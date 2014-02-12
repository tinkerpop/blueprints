/**
 *
 */
package com.tinkerpop.blueprints.impls.sparksee;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.WrappingCloseableIterable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link Vertex} implementation for Sparksee.
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
class SparkseeVertex extends SparkseeElement implements Vertex {
    /**
     * Creates a new instance.
     *
     * @param g   SparkseeGraph.
     * @param oid Sparksee OID.
     */
    protected SparkseeVertex(final SparkseeGraph g, final long oid) {
        super(g, oid);
    }

    public CloseableIterable<Edge> getEdges(final Direction direction, final String... labels) {
        graph.autoStartTransaction();

        if (direction.equals(Direction.OUT)) {
            return this.getOutEdges(labels);
        } else if (direction.equals(Direction.IN))
            return this.getInEdges(labels);
        else {
            return new MultiIterable<Edge>(new ArrayList<Iterable<Edge>>(Arrays.asList(this.getInEdges(labels), this.getOutEdges(labels))));
        }
    }

    public CloseableIterable<Vertex> getVertices(final Direction direction, final String... labels) {
        graph.autoStartTransaction();

        if (direction.equals(Direction.OUT)) {
            return this.getOutVertices(labels);
        } else if (direction.equals(Direction.IN))
            return this.getInVertices(labels);
        else {
            return new MultiIterable<Vertex>(new ArrayList<Iterable<Vertex>>(Arrays.asList(this.getInVertices(labels), this.getOutVertices(labels))));
        }
    }

    private CloseableIterable<Edge> getOutEdgesNoLabels() {
        com.sparsity.sparksee.gdb.TypeList tlist = graph.getRawGraph().findEdgeTypes();
        final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
        for (Integer etype : tlist) {
            edges.add(getOutEdgesSingleType(etype));
        }
        tlist.delete();
        tlist = null;
        return new MultiIterable<Edge>(edges);
    }

    private CloseableIterable<Vertex> getOutVerticesNoLabels() {
        com.sparsity.sparksee.gdb.TypeList tlist = graph.getRawGraph().findEdgeTypes();
        final List<Iterable<Vertex>> vertices = new ArrayList<Iterable<Vertex>>();
        for (Integer etype : tlist) {
            vertices.add(getOutVerticesSingleType(etype));
        }
        tlist.delete();
        tlist = null;
        return new MultiIterable<Vertex>(vertices);
    }

    private CloseableIterable<Edge> getInEdgesNoLabels() {
        com.sparsity.sparksee.gdb.TypeList tlist = graph.getRawGraph().findEdgeTypes();
        final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
        for (Integer etype : tlist) {
            edges.add(getInEdgesSingleType(etype));
        }
        tlist.delete();
        tlist = null;
        return new MultiIterable<Edge>(edges);
    }

    private CloseableIterable<Vertex> getInVerticesNoLabels() {
        com.sparsity.sparksee.gdb.TypeList tlist = graph.getRawGraph().findEdgeTypes();
        final List<Iterable<Vertex>> vertices = new ArrayList<Iterable<Vertex>>();
        for (Integer etype : tlist) {
            vertices.add(getInVerticesSingleType(etype));
        }
        tlist.delete();
        tlist = null;
        return new MultiIterable<Vertex>(vertices);
    }

    private CloseableIterable<Edge> getOutEdgesSingleLabel(final String label) {
        int type = graph.getRawGraph().findType(label);
        if (type == com.sparsity.sparksee.gdb.Type.InvalidType) {
            return new WrappingCloseableIterable<Edge>((Iterable) Collections.emptyList());
        }

        return getOutEdgesSingleType(type);
    }

    private CloseableIterable<Vertex> getOutVerticesSingleLabel(final String label) {
        int type = graph.getRawGraph().findType(label);
        if (type == com.sparsity.sparksee.gdb.Type.InvalidType) {
            return new WrappingCloseableIterable<Vertex>((Iterable) Collections.emptyList());
        }

        return getOutVerticesSingleType(type);
    }

    private CloseableIterable<Edge> getOutEdgesSingleType(final int type) {
        com.sparsity.sparksee.gdb.Objects objs = graph.getRawGraph().explode(oid, type, com.sparsity.sparksee.gdb.EdgesDirection.Outgoing);
        return new SparkseeIterable<Edge>(graph, objs, Edge.class);
    }

    private CloseableIterable<Vertex> getOutVerticesSingleType(final int type) {
        com.sparsity.sparksee.gdb.Objects objs = graph.getRawGraph().neighbors(oid, type, com.sparsity.sparksee.gdb.EdgesDirection.Outgoing);
        return new SparkseeIterable<Vertex>(graph, objs, Vertex.class);
    }

    private CloseableIterable<Edge> getInEdgesSingleLabel(final String label) {
        int type = graph.getRawGraph().findType(label);
        if (type == com.sparsity.sparksee.gdb.Type.InvalidType) {
            return new WrappingCloseableIterable<Edge>((Iterable) Collections.emptyList());
        }

        return getInEdgesSingleType(type);
    }

    private CloseableIterable<Vertex> getInVerticesSingleLabel(final String label) {
        int type = graph.getRawGraph().findType(label);
        if (type == com.sparsity.sparksee.gdb.Type.InvalidType) {
            return new WrappingCloseableIterable<Vertex>((Iterable) Collections.emptyList());
        }

        return getInVerticesSingleType(type);
    }

    private CloseableIterable<Edge> getInEdgesSingleType(final int type) {
        com.sparsity.sparksee.gdb.Objects objs = graph.getRawGraph().explode(oid, type, com.sparsity.sparksee.gdb.EdgesDirection.Ingoing);
        return new SparkseeIterable<Edge>(graph, objs, Edge.class);
    }

    private CloseableIterable<Vertex> getInVerticesSingleType(final int type) {
        com.sparsity.sparksee.gdb.Objects objs = graph.getRawGraph().neighbors(oid, type, com.sparsity.sparksee.gdb.EdgesDirection.Ingoing);
        return new SparkseeIterable<Vertex>(graph, objs, Vertex.class);
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    private CloseableIterable<Edge> getInEdges(final String... labels) {
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

    private CloseableIterable<Vertex> getInVertices(final String... labels) {
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

    private CloseableIterable<Edge> getOutEdges(final String... labels) {
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

    private CloseableIterable<Vertex> getOutVertices(final String... labels) {
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

    public VertexQuery query() {
        return new DefaultVertexQuery(this);
    }

    public Edge addEdge(final String label, final Vertex vertex) {
        return this.graph.addEdge(null, this, vertex, label);
    }
}