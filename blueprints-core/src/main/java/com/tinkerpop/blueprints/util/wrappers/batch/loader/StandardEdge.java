package com.tinkerpop.blueprints.util.wrappers.batch.loader;

import java.util.Map;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class StandardEdge extends StandardTriple implements EdgeTriple {


    public StandardEdge(final Object outVertexId, final String type, final Object inVertexId) {
        super(outVertexId, type, inVertexId);
    }

    public StandardEdge(final Object outVertexId, final String type, final Object inVertexId, final String[] propertykeys, final Object[] propertyValues) {
        super(outVertexId, type, inVertexId, propertykeys, propertyValues);
    }

    public StandardEdge(final Object outVertexId, final String type, final Object inVertexId, final Map<String, Object> properties) {
        super(outVertexId, type, inVertexId, properties);
    }

    @Override
    public Object getInVertexId() {
        return super.getInObject();
    }

    @Override
    public boolean isEdge() {
        return true;
    }
}
