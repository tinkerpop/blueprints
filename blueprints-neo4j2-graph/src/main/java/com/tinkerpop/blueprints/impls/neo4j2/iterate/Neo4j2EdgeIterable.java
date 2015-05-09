package com.tinkerpop.blueprints.impls.neo4j2.iterate;

import org.neo4j.graphdb.Relationship;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;

public class Neo4j2EdgeIterable  extends Neo4j2ElementIterable<Edge, Relationship>{

	public Neo4j2EdgeIterable(Iterable<Relationship> relationships, Neo4j2Graph graph) {
		super(relationships, graph, graph.getEdgeWrapper());
	}
	
}
