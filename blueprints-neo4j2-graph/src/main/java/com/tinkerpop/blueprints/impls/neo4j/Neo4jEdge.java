package com.tinkerpop.blueprints.impls.neo4j;


import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ExceptionFactory;
import com.tinkerpop.blueprints.util.StringFactory;
import org.neo4j.graphdb.Relationship;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jEdge extends Neo4jElement implements Edge {

    public Neo4jEdge(final Relationship relationship, final Neo4jGraph graph) {
        super(graph);
        this.rawElement = relationship;
    }

    public String getLabel() {
        return ((Relationship) this.rawElement).getType().name();
    }

    public Vertex getVertex(final Direction direction) {
        if (direction.equals(Direction.OUT))
            return new Neo4jVertex(((Relationship) this.rawElement).getStartNode(), this.graph);
        else if (direction.equals(Direction.IN))
            return new Neo4jVertex(((Relationship) this.rawElement).getEndNode(), this.graph);
        else
            throw ExceptionFactory.bothIsNotSupported();

    }

    public boolean equals(final Object object) {
        return object instanceof Neo4jEdge && ((Neo4jEdge) object).getId().equals(this.getId());
    }

    public String toString() {
        return StringFactory.edgeString(this);
    }

    public Relationship getRawEdge() {
        return (Relationship) this.rawElement;
    }
}
