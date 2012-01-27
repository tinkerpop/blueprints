/**
 *
 */
package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;

/**
 * {@link Edge} implementation for Dex.
 * <p/>
 * It computes "in vertex" and "out vertex" just when it is necessary.
 * <p/>
 * Since edges are labeled, {@link #getLabel()} gets the same result as
 * {@link #getProperty(String)} if and only if the key is
 * {@link DexElement#LABEL_PROPERTY}.
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
      * @see com.tinkerpop.blueprints.pgm.Edge#getOutVertex()
      */
    @Override
    public Vertex getOutVertex() {
        setEdges();
        return new DexVertex(graph, out);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Edge#getInVertex()
      */
    @Override
    public Vertex getInVertex() {
        setEdges();
        return new DexVertex(graph, in);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Edge#getLabel()
      */
    @Override
    public String getLabel() {
        return getTypeLabel();
    }

    public String toString() {
        return StringFactory.edgeString(this);
    }
}
