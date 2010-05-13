package com.tinkerpop.blueprints.pgm.impls.neo4j;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jEdgeSequence;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jVertex extends Neo4jElement implements Vertex {

    public Neo4jVertex(final Node node, final Neo4jGraph graph) {
        super(graph);
        this.element = node;

    }

    public Iterable<Edge> getOutEdges() {
        return new Neo4jEdgeSequence(((Node) this.element).getRelationships(Direction.OUTGOING), this.graph);
    }

    public Iterable<Edge> getInEdges() {
        return new Neo4jEdgeSequence(((Node) this.element).getRelationships(Direction.INCOMING), this.graph);
    }

    public boolean equals(final Object object) {
        return object instanceof Neo4jVertex && ((Neo4jVertex) object).getId().equals(this.getId());
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }
}
