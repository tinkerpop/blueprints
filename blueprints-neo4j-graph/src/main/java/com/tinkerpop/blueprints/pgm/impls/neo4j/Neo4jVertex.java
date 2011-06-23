package com.tinkerpop.blueprints.pgm.impls.neo4j;


import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.MultiIterable;
import com.tinkerpop.blueprints.pgm.impls.StringFactory;
import com.tinkerpop.blueprints.pgm.impls.neo4j.util.Neo4jEdgeSequence;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;

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
            return new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(Direction.INCOMING), this.graph);
        else if (labels.length == 1) {
            return new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(DynamicRelationshipType.withName(labels[0]), Direction.INCOMING), this.graph);
        } else {
            final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            for (final String label : labels) {
                edges.add(new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(DynamicRelationshipType.withName(label), Direction.INCOMING), this.graph));
            }
            return new MultiIterable<Edge>(edges);
        }
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        if (labels.length == 0)
            return new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(Direction.OUTGOING), this.graph);
        else if (labels.length == 1) {
            return new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(DynamicRelationshipType.withName(labels[0]), Direction.OUTGOING), this.graph);
        } else {
            final List<Iterable<Edge>> edges = new ArrayList<Iterable<Edge>>();
            for (final String label : labels) {
                edges.add(new Neo4jEdgeSequence(((Node) this.rawElement).getRelationships(DynamicRelationshipType.withName(label), Direction.OUTGOING), this.graph));
            }
            return new MultiIterable<Edge>(edges);
        }
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
