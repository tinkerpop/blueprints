package com.tinkerpop.blueprints.impls.orient;

import java.util.Set;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;

/**
 * @author Luca Garulli (http://www.orientechnologies.com)
 */
public class OrientEdge extends OrientElement implements Edge {

    public OrientEdge() {
        super(null, new ODocument());
    }


    public OrientEdge(final OrientGraph rawGraph, final ODocument rawEdge, final String label) {
        super(rawGraph, rawEdge);
        this.rawElement.field(StringFactory.LABEL, label);
    }
    
    public OrientEdge(final OrientGraph rawGraph, final ODocument rawEdge) {
        super(rawGraph, rawEdge);
    }

    public Vertex getVertex(final Direction direction) {
        if (direction.equals(Direction.OUT))
            return new OrientVertex(graph, graph.getRawGraph().getOutVertex(rawElement));
        else if (direction.equals(Direction.IN))
            return new OrientVertex(graph, graph.getRawGraph().getInVertex(rawElement));
        else
            throw ExceptionFactory.bothIsNotSupported();
    }

    public String getLabel() {
        return (String) this.rawElement.field(StringFactory.LABEL);
    }

    public Set<String> getPropertyKeys() {
        final Set<String> set = super.getPropertyKeys();
        set.remove(OGraphDatabase.EDGE_FIELD_IN);
        set.remove(OGraphDatabase.EDGE_FIELD_OUT);
        return set;
    }

    public String toString() {
        return StringFactory.edgeString(this);
    }
}