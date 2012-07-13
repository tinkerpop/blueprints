package com.tinkerpop.blueprints.impls.datomic;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
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
        this.id = id;
    }

    @Override
    public Vertex getVertex(Direction direction) throws IllegalArgumentException {
        if (direction.equals(Direction.OUT))
            return new DatomicVertex(graph, graph.getRawGraph().datoms(Database.EAVT, getId(), graph.GRAPH_EDGE_OUT_VERTEX).iterator().next().v());
        else if (direction.equals(Direction.IN))
            return new DatomicVertex(graph, graph.getRawGraph().datoms(Database.EAVT, getId(), graph.GRAPH_EDGE_IN_VERTEX).iterator().next().v());
        else
            throw ExceptionFactory.bothIsNotSupported();
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
