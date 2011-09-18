package com.tinkerpop.blueprints.pgm.impls.neo4j;


import com.tinkerpop.blueprints.pgm.AutomaticIndex;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import org.neo4j.graphdb.Relationship;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jEdge extends Neo4jElement implements Edge {

    public Neo4jEdge(final Relationship relationship, final Neo4jGraph graph) {
        this(relationship, graph, false);
    }

    protected Neo4jEdge(final Relationship relationship, final Neo4jGraph graph, boolean isNew) {
        super(graph);
        this.rawElement = relationship;
        if (isNew) {
            for (final Neo4jAutomaticIndex autoIndex : this.graph.getAutoIndices(Neo4jEdge.class)) {
                autoIndex.autoUpdate(AutomaticIndex.LABEL, this.getLabel(), null, this);
            }
        }
    }

    public String getLabel() {
        return ((Relationship) this.rawElement).getType().name();
    }

    public Vertex getOutVertex() {
        return new Neo4jVertex(((Relationship) this.rawElement).getStartNode(), this.graph);
    }

    public Vertex getInVertex() {
        return new Neo4jVertex(((Relationship) this.rawElement).getEndNode(), this.graph);
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
