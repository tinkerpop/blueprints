package com.tinkerpop.blueprints.pgm.impls.datomic;

import clojure.lang.Keyword;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import datomic.Peer;
import datomic.Util;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicEdge extends DatomicElement implements Edge {

    public DatomicEdge(final DatomicGraph graph) {
        super(graph);
        graph.addToTransaction(Util.map(":db/id", datomicId,
                                        ":graph.element/type", ":graph.element.type/edge",
                                        ":db/ident", uuid));
    }

    public DatomicEdge(final DatomicGraph graph, final Object id) {
        super(graph);
        if (id instanceof Keyword) {
            uuid = id;
            datomicId = graph.getRawGraph().entid(uuid);
        }
        else {
            throw new RuntimeException(ID_EXCEPTION_MESSAGE);
        }
    }

    public Vertex getOutVertex() {
        return new DatomicVertex(graph, Peer.q("[:find ?uuid " +
                                                ":in $ ?edge " +
                                                ":where [?edge :graph.edge/outVertex ?vertex] " +
                                                       "[?vertex :db/ident ?uuid] ]", graph.getRawGraph(), datomicId).iterator().next().get(0));
    }

    public Vertex getInVertex() {
        return new DatomicVertex(graph, Peer.q("[:find ?uuid " +
                                                ":in $ ?edge " +
                                                ":where [?edge :graph.edge/inVertex ?vertex] " +
                                                       "[?vertex :db/ident ?uuid] ]", graph.getRawGraph(), datomicId).iterator().next().get(0));
    }

    public String getLabel() {
       return (String)Peer.q("[:find ?label " +
                              ":in $ ?edge " +
                              ":where [?edge :graph.edge/label ?label] ]", graph.getRawGraph(), datomicId).iterator().next().get(0);
    }

    public String toString() {
        return StringFactory.edgeString(this);
    }

    public boolean equals(final Object object) {
        return object instanceof DatomicEdge && ((DatomicEdge)object).getId().equals(this.getId());
    }

}
