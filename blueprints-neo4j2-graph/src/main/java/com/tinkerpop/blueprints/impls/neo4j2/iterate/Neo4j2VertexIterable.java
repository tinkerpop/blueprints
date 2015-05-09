package com.tinkerpop.blueprints.impls.neo4j2.iterate;

import org.neo4j.graphdb.Node;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;

public class Neo4j2VertexIterable extends Neo4j2ElementIterable<Vertex, Node>{
	
	public Neo4j2VertexIterable(Iterable<Node> nodes,Neo4j2Graph graph) {
		super(nodes, graph, graph.getVertexWrapper());
	}

}
