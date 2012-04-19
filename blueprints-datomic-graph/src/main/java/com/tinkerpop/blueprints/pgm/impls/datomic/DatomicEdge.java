package com.tinkerpop.blueprints.pgm.impls.datomic;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import datomic.Database;
import datomic.Util;

/**
 * @author Davy Suvee (http://datablend.be)
 */
public class DatomicEdge extends DatomicElement implements Edge {

    public DatomicEdge(final DatomicGraph graph) {
        super(graph);
        graph.addToTransaction(Util.map(":db/id", id,
                                        ":graph.element/type", ":graph.element.type/edge",
                                        ":db/ident", uuid));
    }

    public DatomicEdge(final DatomicGraph graph, final Object id) {
        super(graph);
        if (id instanceof Long) {
            this.id = id;
        }
        else {
            throw new RuntimeException(ID_EXCEPTION_MESSAGE);
        }
    }

    public Vertex getOutVertex() {
        // Use the raw index to retrieve the out vertex of an particular edge
        return new DatomicVertex(graph, graph.getRawGraph().datoms(Database.EAVT, getId(), graph.GRAPH_EDGE_OUT_VERTEX).iterator().next().v());
    }

    public Vertex getInVertex() {
        // Use the raw index to retrieve the in vertex of an particular edge
        return new DatomicVertex(graph, graph.getRawGraph().datoms(Database.EAVT, getId(), graph.GRAPH_EDGE_IN_VERTEX).iterator().next().v());
    }

    public String getLabel() {
       return (String)graph.getRawGraph().datoms(Database.EAVT, getId(), graph.GRAPH_EDGE_LABEL).iterator().next().v();
    }

    public String toString() {
        return StringFactory.edgeString(this);
    }

    public boolean equals(final Object object) {
        return object instanceof DatomicEdge && ((DatomicEdge)object).getId().equals(this.getId());
    }

}
