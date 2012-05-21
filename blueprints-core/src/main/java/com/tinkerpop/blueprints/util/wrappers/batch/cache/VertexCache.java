package com.tinkerpop.blueprints.util.wrappers.batch.cache;

import com.tinkerpop.blueprints.Vertex;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public interface VertexCache {

    public Vertex getVertex(Object externalID);

    public void add(Vertex vertex, Object externalID);

    public void newTransaction();

}
