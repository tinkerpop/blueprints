package com.tinkerpop.blueprints.util.wrappers.batch.cache;

import com.tinkerpop.blueprints.Vertex;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public interface VertexCache {

    public Object getEntry(Object externalId);

    public void set(Vertex vertex, Object externalId);

    public void setId(Object vertexId, Object externalId);

    public boolean contains(Object externalId);

    public void newTransaction();

}