/**
 *
 */
package com.tinkerpop.blueprints.impls.sparksee;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;

/**
 * {@link Edge} implementation for Sparksee.
 * <p/>
 * It computes "in vertex" and "out vertex" just when it is necessary.
 * <p/>
 * Since edges are labeled, {@link #getLabel()} gets the same result as
 * {@link #getProperty(String)} if and only if the key is
 * {@link StringFactory#LABEL}.
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
class SparkseeEdge extends SparkseeElement implements Edge {

    /**
     * In vertex.
     *
     * @see #setEdges()
     */
    private long in = com.sparsity.sparksee.gdb.Objects.InvalidOID;

    /**
     * Out vertex.
     *
     * @see #setEdges()
     */
    private long out = com.sparsity.sparksee.gdb.Objects.InvalidOID;

    /**
     * Sets in vertex and out vertex in case they have not been set before.
     */
    private void setEdges() {
        if (in == com.sparsity.sparksee.gdb.Objects.InvalidOID || out == com.sparsity.sparksee.gdb.Objects.InvalidOID) {
            com.sparsity.sparksee.gdb.EdgeData edata = graph.getRawGraph().getEdgeData(oid);
            out = edata.getTail();
            in = edata.getHead();
            edata = null;
        }
    }

    /**
     * Creates a new instance.
     *
     * @param g   SparkseeGraph.
     * @param oid Sparksee OID.
     */
    protected SparkseeEdge(final SparkseeGraph g, final long oid) {
        super(g, oid);
        this.in = com.sparsity.sparksee.gdb.Objects.InvalidOID;
        this.out = com.sparsity.sparksee.gdb.Objects.InvalidOID;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Edge#getVertex(Direction.OUT)
      */
    @Override
    public Vertex getVertex(final Direction direction) {
        graph.autoStartTransaction();

        setEdges();
        if (direction.equals(Direction.OUT))
            return new SparkseeVertex(graph, out);
        else if (direction.equals(Direction.IN))
            return new SparkseeVertex(graph, in);
        else
            throw ExceptionFactory.bothIsNotSupported();
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Edge#getLabel()
      */
    @Override
    public String getLabel() {
        graph.autoStartTransaction();

        return getTypeLabel();
    }

    public String toString() {
        graph.autoStartTransaction();

        return StringFactory.edgeString(this);
    }
}