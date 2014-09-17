package com.tinkerpop.blueprints.impls.sparksee;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.Element;
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
    
    private static final com.sparsity.sparksee.gdb.EdgesDirection SPARKSEE_IN   = com.sparsity.sparksee.gdb.EdgesDirection.Ingoing;
    private static final com.sparsity.sparksee.gdb.EdgesDirection SPARKSEE_OUT  = com.sparsity.sparksee.gdb.EdgesDirection.Outgoing;

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
        graph.autoStartTransaction(false);

        if (direction.equals(Direction.OUT)) {
            return this.getIterable(Edge.class, SPARKSEE_OUT, labels);
        } else if (direction.equals(Direction.IN)) {
            return this.getIterable(Edge.class, SPARKSEE_IN, labels);
        } else {
            return new MultiIterable<Edge>(new ArrayList<Iterable<Edge>>(
                    Arrays.asList(this.getIterable(Edge.class, SPARKSEE_IN, labels), 
                                  this.getIterable(Edge.class, SPARKSEE_OUT, labels))));
        }
    }

    public CloseableIterable<Vertex> getVertices(final Direction direction, final String... labels) {
        graph.autoStartTransaction(false);

        if (direction.equals(Direction.OUT)) {
            return this.getIterable(Vertex.class, SPARKSEE_OUT, labels);
        } else if (direction.equals(Direction.IN)) {
            return this.getIterable(Vertex.class, SPARKSEE_IN, labels);
        } else {
            return new MultiIterable<Vertex>(new ArrayList<Iterable<Vertex>>(
                    Arrays.asList(this.getIterable(Vertex.class, SPARKSEE_IN, labels),
                                  this.getIterable(Vertex.class, SPARKSEE_OUT, labels))));
        }
    }

    public VertexQuery query() {
        return new DefaultVertexQuery(this);
    }

    public Edge addEdge(final String label, final Vertex vertex) {
        return this.graph.addEdge(null, this, vertex, label);
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    private <T extends Element> CloseableIterable<T> getIterable(Class<T> clazz, final int type,
            com.sparsity.sparksee.gdb.EdgesDirection direction) {
        
        if (type == com.sparsity.sparksee.gdb.Type.InvalidType) {
            return new WrappingCloseableIterable<T>((Iterable) Collections.emptyList());
        }
        
        com.sparsity.sparksee.gdb.Objects objs = null;
        if (clazz == Edge.class) {
            objs = graph.getRawGraph().explode(oid, type, direction);
        } else {
            objs = graph.getRawGraph().neighbors(oid, type, direction);
        }
        return new SparkseeIterable<T>(graph, objs, clazz);
    }
    
    private <T extends Element> CloseableIterable<T> getIterable(Class<T> clazz,
            com.sparsity.sparksee.gdb.EdgesDirection direction, final String... labels) {
        
        if (labels.length == 0) {
            com.sparsity.sparksee.gdb.TypeList tlist = graph.getRawGraph().findEdgeTypes();
            final List<Iterable<T>> elements = new ArrayList<Iterable<T>>();
            for (Integer type : tlist) {
                elements.add(getIterable(clazz, type, direction));
            }
            tlist.delete();
            tlist = null;
            return new MultiIterable<T>(elements);
        } else if (labels.length == 1) {
            int type = graph.getRawGraph().findType(labels[0]);
            return getIterable(clazz, type, direction);
        } else {
            final List<Iterable<T>> elements = new ArrayList<Iterable<T>>();
            for (final String label : labels) {
                int type = graph.getRawGraph().findType(label);
                elements.add(this.getIterable(clazz, type, direction));
            }
            return new MultiIterable<T>(elements);
        }
    }
}