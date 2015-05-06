package com.tinkerpop.blueprints.impls.neo4j2.index;

import java.util.Map;

import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Edge;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.impls.neo4j2.iterate.Neo4j2EdgeIterable;

public class Neo4j2EdgeIndex  extends Neo4j2ElementIndex<Edge, Relationship> {

	protected Neo4j2EdgeIndex(String indexName, Neo4j2Graph graph, Parameter<?,?>... indexParameters) {
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
	public void put(String key, Object value, Edge element) {
		this.graph.autoStartTransaction(true);
		this.rawIndex.add(((Neo4j2Edge)element).getRawEdge(), key, value);
	}

	@Override
	public void remove(String key, Object value, Edge element) {
		this.graph.autoStartTransaction(true);
        this.rawIndex.remove(((Neo4j2Edge)element).getRawEdge(), key, value);
	}

	@Override
	public CloseableIterable<Edge> get(String key, Object value) {
		this.graph.autoStartTransaction(false);
		return new Neo4j2EdgeIterable(this.graph, this.rawIndex.get(key, value));
	}

	@Override
	public CloseableIterable<Edge> query(String key, Object query) {
		this.graph.autoStartTransaction(false);
		return new Neo4j2EdgeIterable(this.graph, this.rawIndex.query(key, query));
	}

}
