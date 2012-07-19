package com.tinkerpop.blueprints.util.wrappers.batch.cache;

import com.tinkerpop.blueprints.Vertex;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public interface VertexCache {

    public Object getEntry(Object externalID);

    public void set(Vertex vertex, Object externalID);

    public void newTransaction();

}
