package com.tinkerpop.blueprints.impls.neo4j2.index;

import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.iterate.Neo4j2VertexIterable;

public class Neo4j2VertexIndex extends Neo4j2ElementIndex<Vertex, Node> {

	public Neo4j2VertexIndex(String indexName, Neo4j2Graph graph, Parameter<?,?>... indexParameters) {
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
	protected Node getRawElement(Vertex element) {
		return ((Neo4j2Vertex)element).getRawElement();
	}

	@Override
	protected CloseableIterable<Vertex> wrapIndexHits(IndexHits<Node> indexHits) {
		return new Neo4j2VertexIterable(indexHits, this.graph);
	}


}
