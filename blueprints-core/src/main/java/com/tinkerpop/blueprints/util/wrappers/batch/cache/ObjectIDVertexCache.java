package com.tinkerpop.blueprints.util.wrappers.batch.cache;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

import java.util.HashMap;
import java.util.Map;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class ObjectIDVertexCache implements VertexCache {

    private static final int INITIAL_CAPACITY = 1000;

    private final Map<Object, Object> map;

    public ObjectIDVertexCache() {
        map = new HashMap<Object, Object>(INITIAL_CAPACITY);
    }


    @Override
    public Object getEntry(Object externalId) {
        return map.get(externalId);
    }

    @Override
    public void set(Vertex vertex, Object externalId) {
        setId(vertex,externalId);
    }

    @Override
    public void setId(Object vertexId, Object externalId) {
        map.put(externalId, vertexId);
    }

    @Override
    public boolean contains(Object externalId) {
        return map.containsKey(externalId);
    }


    @Override
    public void newTransaction() {
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Vertex) {
                Vertex v = (Vertex) entry.getValue();
                entry.setValue(v.getId());
            }
        }
    }
}