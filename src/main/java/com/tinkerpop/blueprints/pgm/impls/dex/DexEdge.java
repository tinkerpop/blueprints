/**
 *
 */
package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import edu.upc.dama.dex.core.Graph;

/**
 * {@link Edge} implementation for DEX.
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
    private long in = Graph.INVALID_NODE;

    /**
     * Out vertex.
     *
     * @see #setEdges()
     */
    private long out = Graph.INVALID_NODE;

    /**
     * Sets in vertex and out vertex in case they have not been set before.
     */
    private void setEdges() {
        if (in == Graph.INVALID_NODE || out == Graph.INVALID_NODE) {
            long edge[] = graph.getRawGraph().getEdge(oid);
            out = edge[0];
            in = edge[1];
        }
    }

    /**
     * Creates a new instance.
     *
     * @param g   DexGraph.
     * @param oid DEX OID.
     */
    DexEdge(DexGraph g, long oid) {
        super(g, oid);
        this.in = Graph.INVALID_NODE;
        this.out = Graph.INVALID_NODE;
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
}
