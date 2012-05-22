/**
 *
 */
package com.tinkerpop.blueprints.impls.dex;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;

/**
 * {@link Edge} implementation for Dex.
 * <p/>
 * It computes "in vertex" and "out vertex" just when it is necessary.
 * <p/>
 * Since edges are labeled, {@link #getLabel()} gets the same result as
 * {@link #getProperty(String)} if and only if the key is
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class DexEdge extends DexElement implements Edge {

    /**
     * In vertex.
     *
     * @see #setEdges()
     */
    private long in = com.sparsity.dex.gdb.Objects.InvalidOID;

    /**
     * Out vertex.
     *
     * @see #setEdges()
     */
    private long out = com.sparsity.dex.gdb.Objects.InvalidOID;

    /**
     * Sets in vertex and out vertex in case they have not been set before.
     */
    private void setEdges() {
        if (in == com.sparsity.dex.gdb.Objects.InvalidOID || out == com.sparsity.dex.gdb.Objects.InvalidOID) {
            com.sparsity.dex.gdb.EdgeData edata = graph.getRawGraph().getEdgeData(oid);
            out = edata.getTail();
            in = edata.getHead();
            edata = null;
        }
    }

    /**
     * Creates a new instance.
     *
     * @param g   DexGraph.
     * @param oid Dex OID.
     */
    protected DexEdge(final DexGraph g, final long oid) {
        super(g, oid);
        this.in = com.sparsity.dex.gdb.Objects.InvalidOID;
        this.out = com.sparsity.dex.gdb.Objects.InvalidOID;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.Edge#getVertex(Direction.OUT)
      */
    @Override
    public Vertex getVertex(final Direction direction) {
        setEdges();
        if (direction.equals(Direction.OUT))
            return new DexVertex(graph, out);
        else if (direction.equals(Direction.IN))
            return new DexVertex(graph, in);
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
        return getTypeLabel();
    }

    public String toString() {
        return StringFactory.edgeString(this);
    }
}
