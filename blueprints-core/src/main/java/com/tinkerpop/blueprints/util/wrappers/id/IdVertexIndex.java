package com.tinkerpop.blueprints.util.wrappers.id;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class IdVertexIndex implements Index<Vertex> {
    private final Index<Vertex> baseIndex;
    private final IdGraph idGraph;

    public IdVertexIndex(final Index<Vertex> baseIndex, final IdGraph idGraph) {
        if (null == baseIndex) {
            throw new IllegalArgumentException("null base index");
        }
        this.idGraph = idGraph;
        this.baseIndex = baseIndex;
    }

    public String getIndexName() {
        return baseIndex.getIndexName();
    }

    public Class<Vertex> getIndexClass() {
        return baseIndex.getIndexClass();
    }

    public void put(String key, Object value, Vertex element) {
        baseIndex.put(key, value, getBaseElement(element));
    }

    public CloseableIterable<Vertex> get(String key, Object value) {
        return new IdVertexIterable(baseIndex.get(key, value), this.idGraph);
    }

    public CloseableIterable<Vertex> query(String key, Object query) {
        return new IdVertexIterable(baseIndex.query(key, query), this.idGraph);
    }

    public long count(String key, Object value) {
        return baseIndex.count(key, value);
    }

    public void remove(String key, Object value, Vertex element) {
        baseIndex.remove(key, value, getBaseElement(element));
    }

    private Vertex getBaseElement(final Vertex e) {
        return (Vertex) ((IdVertex) e).baseElement;
    }
}
