package com.tinkerpop.blueprints.pgm.impls.datomic;

import clojure.lang.Keyword;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.datomic.util.DatomicUtil;
import datomic.Peer;
import datomic.Util;
import java.util.Iterator;
import java.util.List;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicVertex extends DatomicElement implements Vertex {

    public DatomicVertex(final DatomicGraph graph) {
        super(graph);
        graph.addToTransaction(Util.map(":db/id", datomicId,
                                        ":graph.element/type", ":graph.element.type/vertex",
                                        ":db/ident", uuid));
    }

    public DatomicVertex(final DatomicGraph graph, final Object id) {
        super(graph);
        if (id instanceof Keyword) {
            uuid = id;
            datomicId = graph.getRawGraph().entid(uuid);
        }
        else {
            throw new RuntimeException(ID_EXCEPTION_MESSAGE);
        }
    }

    public Iterable<Edge> getInEdges(final String... labels) {
        if (labels.length == 0) {
            return getInEdges();
        }
        Iterator<List<Object>> edgesit = Peer.q("[:find ?uuid " +
                                                 ":in $ ?vertex [?label ...] " +
                                                 ":where [?edge :graph.edge/inVertex ?vertex] " +
                                                        "[?edge :db/ident ?uuid] " +
                                                        "[?edge :graph.edge/label ?label ] ]", graph.getRawGraph(), datomicId, labels).iterator();
        return DatomicUtil.getEdgeSequence(edgesit, graph);
    }



    public Iterable<Edge> getInEdges() {
        Iterator<List<Object>> edgesit = Peer.q("[:find ?uuid " +
                                                 ":in $ ?vertex " +
                                                 ":where [?edge :graph.edge/inVertex ?vertex] " +
                                                        "[?edge :db/ident ?uuid] ]", graph.getRawGraph(), datomicId).iterator();
        return DatomicUtil.getEdgeSequence(edgesit, graph);
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        if (labels.length == 0) {
            return getOutEdges();
        }
        Iterator<List<Object>> edgesit = Peer.q("[:find ?uuid " +
                                                 ":in $ ?vertex [?label ...] " +
                                                 ":where [?edge :graph.edge/outVertex ?vertex] " +
                                                        "[?edge :db/ident ?uuid] " +
                                                        "[?edge :graph.edge/label ?label ] ]", graph.getRawGraph(), datomicId, labels).iterator();
        return DatomicUtil.getEdgeSequence(edgesit, graph);
    }

    public Iterable<Edge> getOutEdges() {
        Iterator<List<Object>> edgesit = Peer.q("[:find ?uuid " +
                                                 ":in $ ?vertex " +
                                                 ":where [?edge :graph.edge/outVertex ?vertex] " +
                                                        "[?edge :db/ident ?uuid] ]", graph.getRawGraph(), datomicId).iterator();
        return DatomicUtil.getEdgeSequence(edgesit, graph);
    }

    public boolean equals(final Object object) {
        return object instanceof DatomicVertex && ((DatomicVertex)object).getId().equals(this.getId());
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

}
