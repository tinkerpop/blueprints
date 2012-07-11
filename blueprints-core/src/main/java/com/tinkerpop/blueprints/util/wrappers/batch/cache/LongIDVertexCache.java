package com.tinkerpop.blueprints.util.wrappers.batch.cache;

import cern.colt.function.LongObjectProcedure;
import cern.colt.map.AbstractLongObjectMap;
import cern.colt.map.OpenLongObjectHashMap;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class LongIDVertexCache implements VertexCache {

    private static final int INITIAL_CAPACITY = 1000;

    private final Graph graph;
    private final AbstractLongObjectMap map;

    public LongIDVertexCache(final Graph graph) {
        if (graph == null) throw new IllegalArgumentException("Graph expected.");
        this.graph = graph;
        map = new OpenLongObjectHashMap(INITIAL_CAPACITY);
    }

    private static final long getID(Object externalID) {
        if (!(externalID instanceof Number)) throw new IllegalArgumentException("Number expected.");
        return ((Number) externalID).longValue();
    }


    @Override
    public Vertex getVertex(Object externalID) {
        long id = getID(externalID);
        Object o = map.get(id);
        if (o instanceof Vertex) {
            return (Vertex) o;
        } else if (o != null) { //o is the internal id
            Vertex v = graph.getVertex(o);
            map.put(id, o);
            return v;
        } else return null;
    }

    @Override
    public void add(Vertex vertex, Object externalID) {
        long id = getID(externalID);
        assert !map.containsKey(id);
        map.put(id, vertex);
    }

    @Override
    public void newTransaction() {
        map.forEachPair(new LongObjectProcedure() {
            @Override
            public boolean apply(long l, Object o) {
                if (o instanceof Vertex) {
                    map.put(l, ((Vertex) o).getId());
                }
                return true;
            }
        });
    }
}
