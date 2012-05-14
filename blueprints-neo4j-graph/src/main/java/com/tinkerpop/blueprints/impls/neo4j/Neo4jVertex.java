package com.tinkerpop.blueprints.impls.neo4j;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.DefaultQuery;
import com.tinkerpop.blueprints.util.StringFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jVertex extends Neo4jElement implements Vertex {

    public Neo4jVertex(final Node node, final Neo4jGraph graph) {
        super(graph);
        this.rawElement = node;

    }

    public Iterable<Edge> getInEdges(final String... labels) {
        return new Neo4jVertexEdgesIterable(this.graph, (Node) this.rawElement, Direction.INCOMING, labels);
    }

    public Iterable<Edge> getOutEdges(final String... labels) {
        return new Neo4jVertexEdgesIterable(this.graph, (Node) this.rawElement, Direction.OUTGOING, labels);
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

    private class Neo4jVertexEdgesIterable<T extends Edge> implements Iterable<Neo4jEdge> {

        private final Neo4jGraph graph;
        private final Node node;
        private final Direction direction;
        private final DynamicRelationshipType[] labels;

        public Neo4jVertexEdgesIterable(final Neo4jGraph graph, final Node node, final Direction direction, final String... labels) {
            this.graph = graph;
            this.node = node;
            this.direction = direction;
            this.labels = new DynamicRelationshipType[labels.length];
            for (int i = 0; i < labels.length; i++) {
                this.labels[i] = DynamicRelationshipType.withName(labels[i]);
            }
        }

        public Iterator<Neo4jEdge> iterator() {
            final Iterator<Relationship> itty;
            if (labels.length > 0)
                itty = node.getRelationships(direction, labels).iterator();
            else
                itty = node.getRelationships(direction).iterator();

            return new Iterator<Neo4jEdge>() {


                public Neo4jEdge next() {
                    return new Neo4jEdge(itty.next(), graph);
                }

                public boolean hasNext() {
                    return itty.hasNext();
                }

                public void remove() {
                    itty.remove();
                }
            };
        }
    }

}
