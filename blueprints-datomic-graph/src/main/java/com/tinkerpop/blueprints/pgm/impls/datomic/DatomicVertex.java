package com.tinkerpop.blueprints.pgm.impls.datomic;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.datomic.util.DatomicEdgeSequence;
import com.tinkerpop.blueprints.pgm.impls.datomic.util.DatomicUtil;
import datomic.Database;
import datomic.Datom;
import datomic.Peer;
import datomic.Util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicVertex extends DatomicElement implements Vertex {

    public DatomicVertex(final DatomicGraph graph) {
        super(graph);
        graph.addToTransaction(Util.map(":db/id", id,
                                        ":graph.element/type", ":graph.element.type/vertex",
                                        ":db/ident", uuid));
    }

    public DatomicVertex(final DatomicGraph graph, final Object id) {
        super(graph);
        if (id instanceof Long) {
            this.id = id;
        }
        else {
            throw new RuntimeException(ID_EXCEPTION_MESSAGE);
        }
    }

    public Iterable<Edge> getInEdges(final String... labels) {
        if (labels.length == 0) {
            return getInEdges();
        }
        Iterator<List<Object>> edgesit = Peer.q("[:find ?edge " +
                                                 ":in $ ?vertex [?label ...] " +
                                                 ":where [?edge :graph.edge/inVertex ?vertex] " +
                                                        "[?edge :graph.edge/label ?label ] ]", graph.getRawGraph(), id, labels).iterator();
        return DatomicUtil.getEdgeSequence(edgesit, graph);
    }



    public Iterable<Edge> getInEdges() {
        Iterator<Datom> edgesit = graph.getRawGraph().datoms(Database.AVET, graph.GRAPH_EDGE_IN_VERTEX, getId()).iterator();
        List<Object> edges = new ArrayList<Object>();
        while (edgesit.hasNext()) {
            edges.add(edgesit.next().e());
        }
        return new DatomicEdgeSequence(edges, this.graph);
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        if (labels.length == 0) {
            return getOutEdges();
        }
        Iterator<List<Object>> edgesit = Peer.q("[:find ?edge " +
                                                 ":in $ ?vertex [?label ...] " +
                                                 ":where [?edge :graph.edge/outVertex ?vertex] " +
                                                        "[?edge :graph.edge/label ?label ] ]", graph.getRawGraph(), id, labels).iterator();
        return DatomicUtil.getEdgeSequence(edgesit, graph);
    }

    public Iterable<Edge> getOutEdges() {
        Iterator<Datom> edgesit = graph.getRawGraph().datoms(Database.AVET, graph.GRAPH_EDGE_OUT_VERTEX, getId()).iterator();
        List<Object> edges = new ArrayList<Object>();
        while (edgesit.hasNext()) {
            edges.add(edgesit.next().e());
        }
        return new DatomicEdgeSequence(edges, this.graph);
    }

    public boolean equals(final Object object) {
        return object instanceof DatomicVertex && ((DatomicVertex)object).getId().equals(this.getId());
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

}
