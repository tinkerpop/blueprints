/**
 *
 */
package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.dex.util.DexTypes;
import edu.upc.dama.dex.core.Graph;
import edu.upc.dama.dex.core.Objects;

/**
 * {@link Vertex} implementation for DEX.
 *
 * @author <a href="http://www.sparsity-technologies.com">Sparsity
 *         Technologies</a>
 */
public class DexVertex extends DexElement implements Vertex {
    /**
     * Creates a new instance.
     *
     * @param g   DexGraph.
     * @param oid DEX OID.
     */
    DexVertex(DexGraph g, long oid) {
        super(g, oid);
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Vertex#getOutEdges()
      */
    @Override
    public Iterable<Edge> getOutEdges() {
        Objects result = new Objects(graph.getRawGraph().getSession());
        for (Integer etype : graph.getRawGraph().edgeTypes()) {
            Objects objs = graph.getRawGraph().explode(oid, etype,
                    Graph.EDGES_OUT);
            result.union(objs);
            objs.close();
        }
        Iterable<Edge> ret = new DexIterable<Edge>(graph, result, Edge.class);
        return ret;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Vertex#getInEdges()
      */
    @Override
    public Iterable<Edge> getInEdges() {
        Objects result = new Objects(graph.getRawGraph().getSession());
        for (Integer etype : graph.getRawGraph().edgeTypes()) {
            Objects objs = graph.getRawGraph().explode(oid, etype,
                    Graph.EDGES_IN);
            result.union(objs);
            objs.close();
        }
        Iterable<Edge> ret = new DexIterable<Edge>(graph, result, Edge.class);
        return ret;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Vertex#getOutEdges(java.lang.String)
      */
    @Override
    public Iterable<Edge> getOutEdges(String label) {
        int type = DexTypes.getTypeId(graph.getRawGraph(), label);
        if (type == Graph.INVALID_TYPE) {
            throw new IllegalArgumentException("Non-existent edge label" + label);
        }

        Objects objs = graph.getRawGraph().explode(oid, type, Graph.EDGES_OUT);
        Iterable<Edge> ret = new DexIterable<Edge>(graph, objs, Edge.class);
        return ret;
    }

    /*
      * (non-Javadoc)
      *
      * @see com.tinkerpop.blueprints.pgm.Vertex#getInEdges(java.lang.String)
      */
    @Override
    public Iterable<Edge> getInEdges(String label) {
        int type = DexTypes.getTypeId(graph.getRawGraph(), label);
        if (type == Graph.INVALID_TYPE) {
            throw new IllegalArgumentException("Non-existent edge label" + label);
        }

        Objects objs = graph.getRawGraph().explode(oid, type, Graph.EDGES_IN);
        Iterable<Edge> ret = new DexIterable<Edge>(graph, objs, Edge.class);
        return ret;
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }
}
