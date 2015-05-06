package com.tinkerpop.blueprints.impls.neo4j2.index;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.index.IndexHits;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;

public abstract class Neo4j2ElementIndex<T extends Element, S extends PropertyContainer> implements Index<T>{

	private final String indexName;
	protected final Neo4j2Graph graph;
	protected final org.neo4j.graphdb.index.Index<S> rawIndex;
	
	protected Neo4j2ElementIndex(final String indexName, final Neo4j2Graph graph, final Parameter<?,?>... indexParameters) {
        this.graph = graph;
        this.indexName = indexName;
        
        Map<String, String> config = new HashMap<String, String>();
        for (int i = 0; i < indexParameters.length; i++) {
        	config.put(indexParameters[i].getKey().toString(), indexParameters[i].getValue().toString());
		}
        this.rawIndex = createRawIndex(config.size() == 0 ? null : config);
    }
	
	/**
	 * @param customConfig A non-empty map, or null.
	 * @return A new raw Neo4j index object.
	 */
	protected abstract org.neo4j.graphdb.index.Index<S> createRawIndex(Map<String, String> customConfig);
	protected abstract S getRawElement(T element);
	protected abstract CloseableIterable<T> wrapIndexHits(IndexHits<S> indexHits);

	@Override
	public String getIndexName() {
		return this.indexName;
	}
	
	@Override
	public void put(String key, Object value, T element) {
		this.graph.autoStartTransaction(true);
		this.rawIndex.add(getRawElement(element), key, value);
	}
	

	@Override
	public void remove(String key, Object value, T element) {
		this.graph.autoStartTransaction(true);
        this.rawIndex.remove(getRawElement(element), key, value);
	}

	@Override
	public CloseableIterable<T> get(String key, Object value) {
		this.graph.autoStartTransaction(false);
		return wrapIndexHits(this.rawIndex.get(key, value));
	}

	@Override
	public CloseableIterable<T> query(String key, Object query) {
		this.graph.autoStartTransaction(false);
		return wrapIndexHits(this.rawIndex.query(key, query));
	}
	
	public CloseableIterable<T> query(Object query) {
		this.graph.autoStartTransaction(false);
		return wrapIndexHits(this.rawIndex.query(query));
	}
	
	@Override
	public long count(String key, Object value) {
		this.graph.autoStartTransaction(false);
        final IndexHits<?> hits = this.rawIndex.get(key, value);
        final long count = hits.size();
        hits.close();
        return count;
	}
}
