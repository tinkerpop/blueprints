package com.tinkerpop.blueprints.impls.neo4j2.iterate;

import java.util.Iterator;

import org.neo4j.graphdb.Relationship;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Edge;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;

public class Neo4j2EdgeIterable  extends Neo4j2ElementIterable<Edge, Relationship>{

	public Neo4j2EdgeIterable(Neo4j2Graph graph, Iterable<Relationship> relationships) {
		super(graph, relationships);
	}
	

	@Override
	public Iterator<Edge> iterator() {
		final Iterator<Relationship> relationshipIterator = elements.iterator(); 
		return new Iterator<Edge>() {

			@Override
			public boolean hasNext() {
				graph.autoStartTransaction(false);
				return relationshipIterator.hasNext();
			}

			@Override
			public Edge next() {
				graph.autoStartTransaction(false);
				return new Neo4j2Edge(relationshipIterator.next(), graph);
			}
		};
	}
}
