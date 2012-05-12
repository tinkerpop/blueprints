package com.tinkerpop.blueprints.pgm.impls.neo4j;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Query;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.util.DefaultQuery;
import com.tinkerpop.blueprints.pgm.util.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jEdgeIterable;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jVertex extends Neo4jElement implements Vertex {

    public Neo4jVertex(final Node node, final Neo4jGraph graph) {
        super(graph);
        this.rawElement = node;

    }

    public Iterable<Edge> getInEdges(final String... labels) {
        if (labels.length == 0)
            return new Neo4jEdgeIterable(((Node) this.rawElement).getRelationships(Direction.INCOMING), this.graph);
        else if (labels.length == 1) {
            return new Neo4jEdgeIterable(((Node) this.rawElement).getRelationships(DynamicRelationshipType.withName(labels[0]), Direction.INCOMING), this.graph);
        } else {
            final List<RelationshipType> edgeLabels = new ArrayList<RelationshipType>();
            for (final String label : labels) {
                edgeLabels.add(DynamicRelationshipType.withName(label));
            }
            return new Neo4jEdgeIterable(((Node) this.rawElement).getRelationships(Direction.INCOMING, edgeLabels.toArray(new RelationshipType[edgeLabels.size()])), this.graph);
        }
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        if (labels.length == 0)
            return new Neo4jEdgeIterable(((Node) this.rawElement).getRelationships(Direction.OUTGOING), this.graph);
        else if (labels.length == 1) {
            return new Neo4jEdgeIterable(((Node) this.rawElement).getRelationships(DynamicRelationshipType.withName(labels[0]), Direction.OUTGOING), this.graph);
        } else {
            final List<RelationshipType> edgeLabels = new ArrayList<RelationshipType>();
            for (final String label : labels) {
                edgeLabels.add(DynamicRelationshipType.withName(label));
            }
            return new Neo4jEdgeIterable(((Node) this.rawElement).getRelationships(Direction.OUTGOING, edgeLabels.toArray(new RelationshipType[edgeLabels.size()])), this.graph);
        }
    }

    public Query query() {
        return new DefaultQuery(this);
    }

    public boolean equals(final Object object) {
        return object instanceof Neo4jVertex && ((Neo4jVertex) object).getId().equals(this.getId());
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    public Node getRawVertex() {
        return (Node) this.rawElement;
    }

}
