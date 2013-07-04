package com.tinkerpop.blueprints.util.wrappers.batch.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.tinkerpop.blueprints.Vertex;

abstract class AbstractIDVertexCache implements VertexCache {

    static final int INITIAL_CAPACITY = 1000;
    static final int INITIAL_TX_CAPACITY = 100;

    private final Map<Object, Object> map;
    private final Set<Object> mapKeysInCurrentTx;

    AbstractIDVertexCache() {
        map = new HashMap<Object, Object>(INITIAL_CAPACITY);
        mapKeysInCurrentTx = new HashSet<Object>(INITIAL_TX_CAPACITY);
    }

    @Override
    public Object getEntry(Object externalId) {
        return map.get(externalId);
    }

    @Override
    public void set(Vertex vertex, Object externalId) {
        setId(vertex, externalId);
    }

    @Override
    public void setId(Object vertexId, Object externalId) {
        map.put(externalId, vertexId);
        mapKeysInCurrentTx.add(externalId);
    }

    @Override
    public boolean contains(Object externalId) {
        return map.containsKey(externalId);
    }

    @Override
    public void newTransaction() {
        for (Object id : mapKeysInCurrentTx) {
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