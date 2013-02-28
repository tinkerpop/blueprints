package com.tinkerpop.blueprints.util.wrappers.id;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Index;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdEdgeIndex implements Index<Edge> {
    private final Index<Edge> baseIndex;
    private final IdGraph idGraph;

    public IdEdgeIndex(final Index<Edge> baseIndex, final IdGraph idGraph) {
        if (null == baseIndex) {
            throw new IllegalArgumentException("null base index");
        }
        this.idGraph = idGraph;
        this.baseIndex = baseIndex;
    }

    public String getIndexName() {
        return baseIndex.getIndexName();
    }

    public Class<Edge> getIndexClass() {
        return baseIndex.getIndexClass();
    }

    public void put(String key, Object value, Edge element) {
        baseIndex.put(key, value, getBaseElement(element));
    }

    public CloseableIterable<Edge> get(String key, Object value) {
        return new IdEdgeIterable(baseIndex.get(key, value), this.idGraph);
    }

    public CloseableIterable<Edge> query(String key, Object query) {
        return new IdEdgeIterable(baseIndex.query(key, query), this.idGraph);
    }

    public long count(String key, Object value) {
        return baseIndex.count(key, value);
    }

    public void remove(String key, Object value, Edge element) {
        baseIndex.remove(key, value, getBaseElement(element));
    }

    private Edge getBaseElement(final Edge e) {
        return (Edge) ((IdEdge) e).baseElement;
    }
}
