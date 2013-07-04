package com.tinkerpop.blueprints.util.wrappers.batch.cache;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class StringIDVertexCache implements VertexCache {

    private static final int INITIAL_CAPACITY = 1000;
    private static final int INITIAL_TX_CAPACITY = 100;

    private final Map<String, Object> map;
    private final Set<String> mapKeysInCurrentTx;
    private final StringCompression compression;

    public StringIDVertexCache(final StringCompression compression) {
        if (compression == null) throw new IllegalArgumentException("Compression expected.");
        this.compression = compression;
        map = new HashMap<String, Object>(INITIAL_CAPACITY);
        mapKeysInCurrentTx = new HashSet<String>(INITIAL_TX_CAPACITY);
    }

    public StringIDVertexCache() {
        this(StringCompression.NO_COMPRESSION);
    }

    @Override
    public Object getEntry(Object externalId) {
        String id = compression.compress(externalId.toString());
        return map.get(id);
    }

    @Override
    public void set(Vertex vertex, Object externalId) {
        setId(vertex,externalId);
    }

    @Override
    public void setId(Object vertexId, Object externalId) {
        String id = compression.compress(externalId.toString());
        map.put(id, vertexId);
        mapKeysInCurrentTx.add(id);
    }

    @Override
    public boolean contains(Object externalId) {
        return map.containsKey(compression.compress(externalId.toString()));
    }

    @Override
    public void newTransaction() {
        for (String id : mapKeysInCurrentTx) {
            Object o = map.get(id);
            assert null != o;
            if (o instanceof Vertex) {
                Vertex v = (Vertex)o;
                map.put(id, v.getId());
            }
        }
        mapKeysInCurrentTx.clear();
    }
}