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
    public Object getEntry(Object externalID) {
        long id = getID(externalID);
        return map.get(id);
    }

    @Override
    public void set(Vertex vertex, Object externalID) {
        long id = getID(externalID);
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
