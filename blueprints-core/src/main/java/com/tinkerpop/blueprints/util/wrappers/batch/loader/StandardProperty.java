package com.tinkerpop.blueprints.util.wrappers.batch.loader;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class StandardProperty extends StandardTriple implements PropertyTriple {

    public StandardProperty(final Object outVertexId, final String key, final Object value) {
        super(outVertexId, key, value);
    }

    @Override
    public boolean isEdge() {
        return false;
    }

    @Override
    public Object getProperty() {
        return super.getInObject();
    }
}
