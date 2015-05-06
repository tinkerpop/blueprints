package com.tinkerpop.blueprints.impls.neo4j2.iterate;

import java.util.Iterator;

import org.neo4j.graphdb.Node;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Vertex;

public class Neo4j2VertexIterable extends Neo4j2ElementIterable<Vertex, Node>{
	
	public Neo4j2VertexIterable(Neo4j2Graph graph, Iterable<Node> nodes) {
		super(graph, nodes);
	}
	

	@Override
	public Iterator<Vertex> iterator() {
		final Iterator<Node> nodeIterator = elements.iterator(); 
		return new Iterator<Vertex>() {

			@Override
			public boolean hasNext() {
				graph.autoStartTransaction(false);
				return nodeIterator.hasNext();
			}

			@Override
			public Vertex next() {
				graph.autoStartTransaction(false);
				return new Neo4j2Vertex(nodeIterator.next(), graph);
			}
		};
	}

}
