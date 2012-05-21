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

    private final Graph graph;
    private final Map<Object, Object> map;

    public ObjectIDVertexCache(final Graph graph) {
        if (graph == null) throw new IllegalArgumentException("Graph expected.");
        this.graph = graph;
        map = new HashMap<Object, Object>(INITIAL_CAPACITY);
    }


    @Override
    public Vertex getVertex(Object externalID) {
        Object o = map.get(externalID);
        if (o instanceof Vertex) {
            return (Vertex) o;
        } else if (o != null) { //o is the internal id
            Vertex v = graph.getVertex(o);
            map.put(externalID, o);
            return v;
        } else return null;
    }

    @Override
    public void add(Vertex vertex, Object externalID) {
        assert !map.containsKey(externalID);
        map.put(externalID, vertex);
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
