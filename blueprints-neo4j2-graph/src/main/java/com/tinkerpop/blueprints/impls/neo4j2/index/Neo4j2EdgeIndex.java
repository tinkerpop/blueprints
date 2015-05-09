package com.tinkerpop.blueprints.impls.neo4j2.index;

import java.util.Map;

import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Edge;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.impls.neo4j2.iterate.Neo4j2EdgeIterable;

public class Neo4j2EdgeIndex  extends Neo4j2ElementIndex<Edge, Relationship> {

	public Neo4j2EdgeIndex(String indexName, Neo4j2Graph graph, Parameter<?,?>... indexParameters) {
		super(indexName, graph, indexParameters);
	}
	
	@Override
	protected Index<Relationship> createRawIndex(Map<String, String> customConfig) {
		return this.graph.getRawGraph().index().forRelationships(getIndexName(), customConfig);
	}

	@Override
	public Class<Edge> getIndexClass() {
		return Edge.class;
	}

	@Override
	protected Relationship getRawElement(Edge element) {
		return ((Neo4j2Edge)element).getRawElement();
	}

	@Override
	protected CloseableIterable<Edge> wrapIndexHits(IndexHits<Relationship> indexHits) {
		return new Neo4j2EdgeIterable(indexHits, graph);
	}


}
