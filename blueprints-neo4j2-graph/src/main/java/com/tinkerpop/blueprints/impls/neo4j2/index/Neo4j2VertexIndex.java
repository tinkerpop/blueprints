package com.tinkerpop.blueprints.impls.neo4j2.index;

import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.iterate.Neo4j2VertexIterable;

public class Neo4j2VertexIndex extends Neo4j2ElementIndex<Vertex, Node> {

	protected Neo4j2VertexIndex(String indexName, Neo4j2Graph graph, Parameter<?,?>... indexParameters) {
		super(indexName, graph, indexParameters);
	}
	
	@Override
	protected Index<Node> createRawIndex(Map<String, String> customConfig) {
		return this.graph.getRawGraph().index().forNodes(getIndexName(), customConfig);
	}

	@Override
	public Class<Vertex> getIndexClass() {
		return Vertex.class;
	}

	@Override
	public void put(String key, Object value, Vertex element) {
		this.graph.autoStartTransaction(true);
		this.rawIndex.add(((Neo4j2Vertex)element).getRawVertex(), key, value);
	}

	@Override
	public void remove(String key, Object value, Vertex element) {
		this.graph.autoStartTransaction(true);
        this.rawIndex.remove(((Neo4j2Vertex)element).getRawVertex(), key, value);
	}

	@Override
	public CloseableIterable<Vertex> get(String key, Object value) {
		this.graph.autoStartTransaction(false);
		return new Neo4j2VertexIterable(this.graph, this.rawIndex.get(key, value));
	}

	@Override
	public CloseableIterable<Vertex> query(String key, Object query) {
		this.graph.autoStartTransaction(false);
		return new Neo4j2VertexIterable(this.graph, this.rawIndex.query(key, query));
	}

}
