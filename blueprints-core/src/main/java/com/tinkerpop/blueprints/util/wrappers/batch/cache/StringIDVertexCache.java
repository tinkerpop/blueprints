package com.tinkerpop.blueprints.util.wrappers.batch.cache;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import java.util.HashMap;
import java.util.Map;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class StringIDVertexCache implements VertexCache {

    private static final int INITIAL_CAPACITY = 1000;

    private final Graph graph;
    private final Map<String, Object> map;
    private final StringCompression compression;

    public StringIDVertexCache(final Graph graph, final StringCompression compression) {
        if (graph == null) throw new IllegalArgumentException("Graph expected.");
        if (compression == null) throw new IllegalArgumentException("Compression expected.");
        this.graph = graph;
        this.compression = compression;
        map = new HashMap<String, Object>(INITIAL_CAPACITY);
    }

    public StringIDVertexCache(final Graph graph) {
        this(graph, StringCompression.NO_COMPRESSION);
    }

    @Override
    public Object getEntry(Object externalID) {
        String id = compression.compress((String) externalID);
        return map.get(id);
    }

    @Override
    public void set(Vertex vertex, Object externalID) {
        String id = compression.compress((String) externalID);
        map.put(id, vertex);
    }

    @Override
    public void newTransaction() {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Vertex) {
                Vertex v = (Vertex) entry.getValue();
                entry.setValue(v.getId());
            }
        }
    }
}
