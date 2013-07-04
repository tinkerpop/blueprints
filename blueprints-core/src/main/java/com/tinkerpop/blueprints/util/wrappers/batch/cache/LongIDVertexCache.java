package com.tinkerpop.blueprints.util.wrappers.batch.cache;

import cern.colt.function.LongProcedure;
import cern.colt.list.AbstractLongList;
import cern.colt.list.LongArrayList;
import cern.colt.map.AbstractLongObjectMap;
import cern.colt.map.OpenLongObjectHashMap;

import com.tinkerpop.blueprints.Vertex;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class LongIDVertexCache implements VertexCache {

    private final AbstractLongObjectMap map;
    private final AbstractLongList mapKeysInCurrentTx;
    private final LongProcedure newTransactionProcedure;

    public LongIDVertexCache() {
        map = new OpenLongObjectHashMap(AbstractIDVertexCache.INITIAL_CAPACITY);
        mapKeysInCurrentTx = new LongArrayList(AbstractIDVertexCache.INITIAL_TX_CAPACITY);
        newTransactionProcedure = new VertexConverterLP();
    }

    private static final long getID(Object externalID) {
        if (!(externalID instanceof Number)) throw new IllegalArgumentException("Number expected.");
        return ((Number) externalID).longValue();
    }

    @Override
    public Object getEntry(Object externalId) {
        return map.get(getID(externalId));
    }

    @Override
    public void set(Vertex vertex, Object externalId) {
        setId(vertex,externalId);
    }

    @Override
    public void setId(Object vertexId, Object externalId) {
        long id = getID(externalId);
        map.put(id, vertexId);
        mapKeysInCurrentTx.add(id);
    }

    @Override
    public boolean contains(Object externalId) {
        return map.containsKey(getID(externalId));
    }

    @Override
    public void newTransaction() {
        mapKeysInCurrentTx.forEach(newTransactionProcedure);
        mapKeysInCurrentTx.clear();
    }

    /**
     * See {@link LongIDVertexCache#newTransaction()}
     */
    private class VertexConverterLP implements LongProcedure {
        
        /**
         * Retrieve the Object associated with each long from {@code map}. If it
         * is an {@code instanceof Vertex}, then replace it in the map with
         * {@link Vertex#getId()}. Otherwise, do nothing. Always returns true.
         * 
         * @param l
         *            Colt long list element
         * @return true
         */
        @Override
        public final boolean apply(final long l) {
            final Object o = map.get(l);
            assert null != o;
            if (o instanceof Vertex) {
                map.put(l, ((Vertex) o).getId());
            }
            return true; // tell forEach to apply us to next long
        }
    }
}