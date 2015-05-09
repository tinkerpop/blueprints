package com.tinkerpop.blueprints.impls.neo4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph.ElementWrapper;
import com.tinkerpop.blueprints.impls.neo4j2.iterate.Neo4j2EdgeIterable;
import com.tinkerpop.blueprints.impls.neo4j2.iterate.Neo4j2ElementIterable;
import com.tinkerpop.blueprints.util.DefaultVertexQuery;
import com.tinkerpop.blueprints.util.MultiIterable;
import com.tinkerpop.blueprints.util.StringFactory;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Neo4j2Vertex extends Neo4j2Element<Node> implements Vertex {

	private ElementWrapper<Neo4j2Vertex, Relationship> adjacentVertexWrapper = null;
	
	public Neo4j2Vertex(final Node node, final Neo4j2Graph graph) {
		super(graph);
		this.rawElement = node;
	}


	public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
		if (direction.equals(Direction.OUT) || direction.equals(Direction.IN)){
			return new Neo4j2EdgeIterable(getRelationships(direction, labels), graph);
		} else {
			return new MultiIterable<Edge>(Arrays.asList((Iterable<Edge>) 
					new Neo4j2EdgeIterable(getRelationships(Direction.OUT, labels), graph),
					new Neo4j2EdgeIterable(getRelationships(Direction.IN, labels), graph)));
		}
	}

	public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
		if (direction.equals(Direction.OUT) || direction.equals(Direction.IN)){
			return new AdjacentVertexIterable(getRelationships(direction, labels), graph);
		} else {
			return new MultiIterable<Vertex>(Arrays.asList((Iterable<Vertex>) 
					new AdjacentVertexIterable(getRelationships(Direction.OUT, labels), graph),
					new AdjacentVertexIterable(getRelationships(Direction.IN, labels), graph)));
		}
	}

	public Edge addEdge(final String label, final Vertex vertex) {
		return this.graph.addEdge(null, this, vertex, label);
	}

	public Collection<String> getLabels() {
		this.graph.autoStartTransaction(false);
		final Collection<String> labels = new ArrayList<String>();
		for (Label label : getRawElement().getLabels()) {
			labels.add(label.name());
		}
		return labels;
	}

	public void addLabel(String label) {
		graph.autoStartTransaction(true);
		getRawElement().addLabel(DynamicLabel.label(label));
	}

	public void removeLabel(String label) {
		graph.autoStartTransaction(true);
		getRawElement().removeLabel(DynamicLabel.label(label));
	}

	public VertexQuery query() {
		this.graph.autoStartTransaction(false);
		return new DefaultVertexQuery(this);
	}
	
	@Override
	public void remove() {
		this.graph.removeVertex(this);
	}

	public boolean equals(final Object object) {
		return object instanceof Neo4j2Vertex && ((Neo4j2Vertex) object).getId().equals(this.getId());
	}

	public String toString() {
		return StringFactory.vertexString(this);
	}

	/**
	 * Deprecated, use getRawElement() instead.
	 * @return The underlying Neo4j Node object.
	 */
	@Deprecated
	public Node getRawVertex() {
		return this.rawElement;
	}


	//-------------------------------------------------------------------------
	// Private helpers ...

	private Iterable<Relationship> getRelationships(final Direction direction, final String... labels){
		this.graph.autoStartTransaction(false);
		if(labels.length > 0){
			return this.rawElement.getRelationships(toRawDirection(direction), toRelationshipTypes(labels));
		} else {
			return this.rawElement.getRelationships(toRawDirection(direction));
		}
	}

	private RelationshipType[] toRelationshipTypes(final String... labels){
		RelationshipType[] edgeLabels = new DynamicRelationshipType[labels.length];
		for (int i = 0; i < labels.length; i++) {
			edgeLabels[i] = DynamicRelationshipType.withName(labels[i]);
		}
		return edgeLabels;
	}

	private org.neo4j.graphdb.Direction toRawDirection(final Direction direction){
		switch (direction) {
		case OUT:
			return org.neo4j.graphdb.Direction.OUTGOING;
		case IN:
			return org.neo4j.graphdb.Direction.INCOMING;
		case BOTH:
			return org.neo4j.graphdb.Direction.BOTH;
		default:
			throw new IllegalArgumentException();
		}
	}
	
	private ElementWrapper<Neo4j2Vertex, Relationship> getAdjacentVertexWrapper(){
		if(adjacentVertexWrapper == null){
			adjacentVertexWrapper = new ElementWrapper<Neo4j2Vertex, Relationship>(){
				@Override
				public Neo4j2Vertex wrap(Relationship relationship) {
					return new Neo4j2Vertex(relationship.getOtherNode(rawElement), graph);
				}
			}; 
		}
		return adjacentVertexWrapper;
	}


	private class AdjacentVertexIterable extends Neo4j2ElementIterable<Vertex, Relationship> {
		public AdjacentVertexIterable(Iterable<Relationship> elements, final Neo4j2Graph graph) {
			super(elements, graph, getAdjacentVertexWrapper());
		}
	}

}
