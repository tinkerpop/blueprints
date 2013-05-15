package com.tinkerpop.blueprints.impls.neo4j;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4jVertex extends Neo4jElement implements Vertex {

    public Neo4jVertex(final Node node, final Neo4jGraph graph) {
        super(graph);
        this.rawElement = node;

    }

    public Iterable<Edge> getEdges(final com.tinkerpop.blueprints.Direction direction, final String... labels) {
        if (direction.equals(com.tinkerpop.blueprints.Direction.OUT))
            return new Neo4jVertexEdgeIterable(this.graph, (Node) this.rawElement, Direction.OUTGOING, labels);
        else if (direction.equals(com.tinkerpop.blueprints.Direction.IN))
            return new Neo4jVertexEdgeIterable(this.graph, (Node) this.rawElement, Direction.INCOMING, labels);
        else
            return new MultiIterable(Arrays.asList(new Neo4jVertexEdgeIterable(this.graph, (Node) this.rawElement, Direction.OUTGOING, labels), new Neo4jVertexEdgeIterable(this.graph, (Node) this.rawElement, Direction.INCOMING, labels)));
    }

    public Iterable<Vertex> getVertices(final com.tinkerpop.blueprints.Direction direction, final String... labels) {
        if (direction.equals(com.tinkerpop.blueprints.Direction.OUT))
            return new Neo4jVertexVertexIterable(this.graph, (Node) this.rawElement, Direction.OUTGOING, labels);
        else if (direction.equals(com.tinkerpop.blueprints.Direction.IN))
            return new Neo4jVertexVertexIterable(this.graph, (Node) this.rawElement, Direction.INCOMING, labels);
        else
            return new MultiIterable(Arrays.asList(new Neo4jVertexVertexIterable(this.graph, (Node) this.rawElement, Direction.OUTGOING, labels), new Neo4jVertexVertexIterable(this.graph, (Node) this.rawElement, Direction.INCOMING, labels)));
    }

    public Edge addEdge(final String label, final Vertex vertex) {
        return this.graph.addEdge(null, this, vertex, label);
    }

    public VertexQuery query() {
        return new DefaultVertexQuery(this);
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

    private class Neo4jVertexVertexIterable<T extends Vertex> implements Iterable<Neo4jVertex> {
        private final Neo4jGraph graph;
        private final Node node;
        private final Direction direction;
        private final DynamicRelationshipType[] labels;

        public Neo4jVertexVertexIterable(final Neo4jGraph graph, final Node node, final Direction direction, final String... labels) {
            this.graph = graph;
            this.node = node;
            this.direction = direction;
            this.labels = new DynamicRelationshipType[labels.length];
            for (int i = 0; i < labels.length; i++) {
                this.labels[i] = DynamicRelationshipType.withName(labels[i]);
            }
        }

        public Iterator<Neo4jVertex> iterator() {
            final Iterator<Relationship> itty;
            if (labels.length > 0)
                itty = node.getRelationships(direction, labels).iterator();
            else
                itty = node.getRelationships(direction).iterator();

            return new Iterator<Neo4jVertex>() {
                public Neo4jVertex next() {
                    return new Neo4jVertex(itty.next().getOtherNode(node), graph);
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

    private class Neo4jVertexEdgeIterable<T extends Edge> implements Iterable<Neo4jEdge> {

        private final Neo4jGraph graph;
        private final Node node;
        private final Direction direction;
        private final DynamicRelationshipType[] labels;

        public Neo4jVertexEdgeIterable(final Neo4jGraph graph, final Node node, final Direction direction, final String... labels) {
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
