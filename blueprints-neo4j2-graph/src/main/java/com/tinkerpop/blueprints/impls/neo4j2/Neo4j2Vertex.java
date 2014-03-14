package com.tinkerpop.blueprints.impls.neo4j2;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.StringFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4j2Vertex extends Neo4j2Element implements Vertex {

    public Neo4j2Vertex(final Node node, final Neo4j2Graph graph) {
        super(graph);
        this.rawElement = node;

    }

    public Iterable<Edge> getEdges(final com.tinkerpop.blueprints.Direction direction, final String... labels) {
        this.graph.autoStartTransaction(false);
        if (direction.equals(com.tinkerpop.blueprints.Direction.OUT))
            return new Neo4jVertexEdgeIterable(this.graph, (Node) this.rawElement, Direction.OUTGOING, labels);
        else if (direction.equals(com.tinkerpop.blueprints.Direction.IN))
            return new Neo4jVertexEdgeIterable(this.graph, (Node) this.rawElement, Direction.INCOMING, labels);
        else
            return new MultiIterable(Arrays.asList(new Neo4jVertexEdgeIterable(this.graph, (Node) this.rawElement, Direction.OUTGOING, labels), new Neo4jVertexEdgeIterable(this.graph, (Node) this.rawElement, Direction.INCOMING, labels)));
    }

    public Iterable<Vertex> getVertices(final com.tinkerpop.blueprints.Direction direction, final String... labels) {
        this.graph.autoStartTransaction(false);
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

    public Collection<String> getLabels() {
        this.graph.autoStartTransaction(false);
        final Collection<String> labels = new ArrayList<String>();
        for (Label label : getRawVertex().getLabels()) {
            labels.add(label.name());
        }
        return labels;
    }

    public void addLabel(String label) {
        graph.autoStartTransaction(true);
        getRawVertex().addLabel(DynamicLabel.label(label));
    }

    public void removeLabel(String label) {
        graph.autoStartTransaction(true);
        getRawVertex().removeLabel(DynamicLabel.label(label));
    }

    public VertexQuery query() {
        this.graph.autoStartTransaction(false);
        return new DefaultVertexQuery(this);
    }

    public boolean equals(final Object object) {
        return object instanceof Neo4j2Vertex && ((Neo4j2Vertex) object).getId().equals(this.getId());
    }

    public String toString() {
        return StringFactory.vertexString(this);
    }

    public Node getRawVertex() {
        return (Node) this.rawElement;
    }

    private class Neo4jVertexVertexIterable<T extends Vertex> implements Iterable<Neo4j2Vertex> {
        private final Neo4j2Graph graph;
        private final Node node;
        private final Direction direction;
        private final DynamicRelationshipType[] labels;

        public Neo4jVertexVertexIterable(final Neo4j2Graph graph, final Node node, final Direction direction, final String... labels) {
            this.graph = graph;
            this.node = node;
            this.direction = direction;
            this.labels = new DynamicRelationshipType[labels.length];
            for (int i = 0; i < labels.length; i++) {
                this.labels[i] = DynamicRelationshipType.withName(labels[i]);
            }
        }

        public Iterator<Neo4j2Vertex> iterator() {
            graph.autoStartTransaction(true);
            final Iterator<Relationship> itty;
            if (labels.length > 0)
                itty = node.getRelationships(direction, labels).iterator();
            else
                itty = node.getRelationships(direction).iterator();

            return new Iterator<Neo4j2Vertex>() {
                public Neo4j2Vertex next() {
                    graph.autoStartTransaction(false);
                    return new Neo4j2Vertex(itty.next().getOtherNode(node), graph);
                }

                public boolean hasNext() {
                    graph.autoStartTransaction(false);
                    return itty.hasNext();
                }

                public void remove() {
                    graph.autoStartTransaction(true);
                    itty.remove();
                }
            };
        }
    }

    private class Neo4jVertexEdgeIterable<T extends Edge> implements Iterable<Neo4j2Edge> {

        private final Neo4j2Graph graph;
        private final Node node;
        private final Direction direction;
        private final DynamicRelationshipType[] labels;

        public Neo4jVertexEdgeIterable(final Neo4j2Graph graph, final Node node, final Direction direction, final String... labels) {
            this.graph = graph;
            this.node = node;
            this.direction = direction;
            this.labels = new DynamicRelationshipType[labels.length];
            for (int i = 0; i < labels.length; i++) {
                this.labels[i] = DynamicRelationshipType.withName(labels[i]);
            }
        }

        public Iterator<Neo4j2Edge> iterator() {
            graph.autoStartTransaction(true);
            final Iterator<Relationship> itty;
            if (labels.length > 0)
                itty = node.getRelationships(direction, labels).iterator();
            else
                itty = node.getRelationships(direction).iterator();

            return new Iterator<Neo4j2Edge>() {
                public Neo4j2Edge next() {
                    graph.autoStartTransaction(false);
                    return new Neo4j2Edge(itty.next(), graph);
                }

                public boolean hasNext() {
                    graph.autoStartTransaction(false);
                    return itty.hasNext();
                }

                public void remove() {
                    graph.autoStartTransaction(true);
                    itty.remove();
                }
            };
        }
    }

}
