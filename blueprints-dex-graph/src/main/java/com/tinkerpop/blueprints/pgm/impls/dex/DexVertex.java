/**
 *
 */
package com.tinkerpop.blueprints.pgm.impls.dex;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.MultiIterable;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.dex.util.DexTypes;
import edu.upc.dama.dex.core.Graph;
import edu.upc.dama.dex.core.Objects;

import java.util.ArrayList;
import java.util.List;

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
    protected DexVertex(final DexGraph g, final long oid) {
        super(g, oid);
    }

    private Iterable<Edge> getOutEdgesNoLabels() {
        Objects result = new Objects(graph.getRawGraph().getSession());
        for (Integer etype : graph.getRawGraph().edgeTypes()) {
            Objects objs = graph.getRawGraph().explode(oid, etype, Graph.EDGES_OUT);
            result.union(objs);
            objs.close();
        }
        Iterable<Edge> ret = new DexIterable<Edge>(graph, result, Edge.class);
        return ret;
    }

    private Iterable<Edge> getInEdgesNoLabels() {
        Objects result = new Objects(graph.getRawGraph().getSession());
        for (Integer etype : graph.getRawGraph().edgeTypes()) {
            Objects objs = graph.getRawGraph().explode(oid, etype, Graph.EDGES_IN);
            result.union(objs);
            objs.close();
        }
        Iterable<Edge> ret = new DexIterable<Edge>(graph, result, Edge.class);
        return ret;
    }

    private Iterable<Edge> getOutEdgesSingleLabel(final String label) {
        int type = DexTypes.getTypeId(graph.getRawGraph(), label);
        if (type == Graph.INVALID_TYPE) {
            return new ArrayList<Edge>();
        }

        Objects objs = graph.getRawGraph().explode(oid, type, Graph.EDGES_OUT);
        Iterable<Edge> ret = new DexIterable<Edge>(graph, objs, Edge.class);
        return ret;
    }

    private Iterable<Edge> getInEdgesSingleLabel(final String label) {
        int type = DexTypes.getTypeId(graph.getRawGraph(), label);
        if (type == Graph.INVALID_TYPE) {
            return new ArrayList<Edge>();
        }

        Objects objs = graph.getRawGraph().explode(oid, type, Graph.EDGES_IN);
        Iterable<Edge> ret = new DexIterable<Edge>(graph, objs, Edge.class);
        return ret;
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    public Iterable<Edge> getInEdges(final String... labels) {
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

    public Iterable<Edge> getOutEdges(final String... labels) {
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
}
