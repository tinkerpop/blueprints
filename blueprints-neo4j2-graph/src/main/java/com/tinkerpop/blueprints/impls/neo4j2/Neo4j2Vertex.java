package com.tinkerpop.blueprints.impls.neo4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.StringFactory;

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
        if (direction.equals(com.tinkerpop.blueprints.Direction.OUT)){
            return new Neo4jVertexEdgeIterable(this.graph, (Node) this.rawElement, Direction.OUTGOING, labels);
        } else if (direction.equals(com.tinkerpop.blueprints.Direction.IN)){
            return new Neo4jVertexEdgeIterable(this.graph, (Node) this.rawElement, Direction.INCOMING, labels);
        } else {
        	List<Iterable<Edge>> iterables = new ArrayList<Iterable<Edge>>();
        	iterables.add(new Neo4jVertexEdgeIterable(this.graph, (Node) this.rawElement, Direction.OUTGOING, labels));
        	iterables.add(new Neo4jVertexEdgeIterable(this.graph, (Node) this.rawElement, Direction.INCOMING, labels));
        	return new MultiIterable<Edge>(iterables);
        }
    }

    public Iterable<Vertex> getVertices(final com.tinkerpop.blueprints.Direction direction, final String... labels) {
        this.graph.autoStartTransaction(false);
        if (direction.equals(com.tinkerpop.blueprints.Direction.OUT)){
            return new Neo4jVertexVertexIterable(this.graph, (Node) this.rawElement, Direction.OUTGOING, labels);
        } else if (direction.equals(com.tinkerpop.blueprints.Direction.IN)){
            return new Neo4jVertexVertexIterable(this.graph, (Node) this.rawElement, Direction.INCOMING, labels);
        } else{
        	List<Iterable<Vertex>> iterables = new ArrayList<Iterable<Vertex>>();
        	iterables.add(new Neo4jVertexVertexIterable(this.graph, (Node) this.rawElement, Direction.OUTGOING, labels));
        	iterables.add(new Neo4jVertexVertexIterable(this.graph, (Node) this.rawElement, Direction.INCOMING, labels));
        	return new MultiIterable<Vertex>(iterables);
        }
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

    //-------------------------------------------------------------------------
    // Iterables
    
    private abstract class Neo4jVertexElementIterable<T extends Element> implements Iterable<T> {
    	protected final Neo4j2Graph graph;
    	protected final Node node;
    	protected final Direction direction;
    	protected final RelationshipType[] labels;

        public Neo4jVertexElementIterable(final Neo4j2Graph graph, final Node node, final Direction direction, final String... labels) {
            this.graph = graph;
            this.node = node;
            this.direction = direction;
            this.labels = new DynamicRelationshipType[labels.length];
            for (int i = 0; i < labels.length; i++) {
                this.labels[i] = DynamicRelationshipType.withName(labels[i]);
            }
        }
    }
    
    
    private class Neo4jVertexVertexIterable extends Neo4jVertexElementIterable<Vertex> {
		public Neo4jVertexVertexIterable(Neo4j2Graph graph, Node node, Direction direction, String[] labels) {
			super(graph, node, direction, labels);
		}

		@Override
		public Iterator<Vertex> iterator() {
			return new Neo4jVertexVertexIterator(graph, node, direction, labels);
		}
    }
    
    private class Neo4jVertexEdgeIterable extends Neo4jVertexElementIterable<Edge> {
		public Neo4jVertexEdgeIterable(Neo4j2Graph graph, Node node, Direction direction, String[] labels) {
			super(graph, node, direction, labels);
		}

		@Override
		public Iterator<Edge> iterator() {
			return new Neo4jVertexEdgeIterator(graph, node, direction, labels);
		}
    }
    
    
    //-------------------------------------------------------------------------
    // Iterators ...
    
    private abstract class Neo4jVertexElementIterator<T extends Element> implements Iterator<T> {
    	
    	protected final Neo4j2Graph graph;
    	protected final Iterator<Relationship> itty;
    	protected final Node node;

    	public Neo4jVertexElementIterator(final Neo4j2Graph graph, final Node node, final Direction direction, final RelationshipType... labels) {
    		this.graph = graph;
    		this.node = node;
    		
    		this.graph.autoStartTransaction(false);
    		if (labels.length > 0){
    			itty = node.getRelationships(direction, labels).iterator();
    		} else {
    			itty = node.getRelationships(direction).iterator();
    		}
    	}
    	
		@Override
		public boolean hasNext() {
			this.graph.autoStartTransaction(false);
			return itty.hasNext();
		}

		@Override
		public T next() {
			this.graph.autoStartTransaction(false);
			return next(itty.next());
		}
		
		protected abstract T next(Relationship edge);

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
    	
    }
    
    private class Neo4jVertexVertexIterator extends Neo4jVertexElementIterator<Vertex> {
		public Neo4jVertexVertexIterator(Neo4j2Graph graph, Node node, Direction direction, RelationshipType[] labels) {
			super(graph, node, direction, labels);
		}

		@Override
		protected Vertex next(Relationship edge) {
			return new Neo4j2Vertex(edge.getOtherNode(this.node), graph);
		}
    }
    
    private class Neo4jVertexEdgeIterator extends Neo4jVertexElementIterator<Edge> {
		public Neo4jVertexEdgeIterator(Neo4j2Graph graph, Node node, Direction direction, RelationshipType[] labels) {
			super(graph, node, direction, labels);
		}

		@Override
		protected Edge next(Relationship edge) {
			return new Neo4j2Edge(edge, graph);
		}
    }

}
