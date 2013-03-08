package com.tinkerpop.blueprints.impls.neo4j.batch;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class Neo4jBatchIndex<T extends Element> implements Index<T> {

    private final Neo4jBatchGraph graph;
    protected final BatchInserterIndex rawIndex;
    private final String name;
    private final Class<T> indexClass;

    public Neo4jBatchIndex(final Neo4jBatchGraph graph, final BatchInserterIndex index, final String name, final Class<T> indexClass) {
        this.graph = graph;
        this.rawIndex = index;
        this.name = name;
        this.indexClass = indexClass;
    }

    public void put(final String key, final Object value, final T element) {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, value);
        this.rawIndex.add((Long) element.getId(), map);
    }

    public CloseableIterable<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.indexClass))
            return (CloseableIterable<T>) new Neo4jBatchVertexIterable(this.graph, this.rawIndex.get(key, value));
        else
            return (CloseableIterable<T>) new Neo4jBatchEdgeIterable(this.graph, this.rawIndex.get(key, value));
    }

    public CloseableIterable<T> query(final String key, final Object query) {
        if (Vertex.class.isAssignableFrom(this.indexClass))
            return (CloseableIterable<T>) new Neo4jBatchVertexIterable(this.graph, this.rawIndex.query(key, query));
        else
            return (CloseableIterable<T>) new Neo4jBatchEdgeIterable(this.graph, this.rawIndex.query(key, query));
    }

    public long count(final String key, final Object value) {
        long count = 0;
        for (final T t : this.get(key, value)) {
            count++;
        }
        return count;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void remove(final String key, final Object value, final T element) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(Neo4jBatchTokens.DELETE_OPERATION_MESSAGE);
    }

    public Class<T> getIndexClass() {
        return this.indexClass;
    }

    public String getIndexName() {
        return this.name;
    }

    /**
     * This is required before querying the index for data.
     * This method is not a standard Index API method and thus, be sure to typecast the index to Neo4jBatchIndex.
     */
    public void flush() {
        this.rawIndex.flush();
    }

    public String toString() {
        return StringFactory.indexString(this);
    }
}
