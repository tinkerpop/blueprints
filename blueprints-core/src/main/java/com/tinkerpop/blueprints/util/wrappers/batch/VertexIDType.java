package com.tinkerpop.blueprints.util.wrappers.batch;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.util.wrappers.batch.cache.*;

/**
 * Type of vertex ids expected by BatchGraph. The default is IdType.OBJECT.
 * Use the IdType that best matches the used vertex id types in order to save memory.
 *
 * @author Matthias Broecheler (http://www.matthiasb.com)
 */
public enum VertexIDType {

    OBJECT, NUMBER, STRING, URL;

    public VertexCache getVertexCache() {
        switch (this) {
            case OBJECT:
                return new ObjectIDVertexCache();
            case NUMBER:
                return new LongIDVertexCache();
            case STRING:
                return new StringIDVertexCache();
            case URL:
                return new StringIDVertexCache(new URLCompression());
            default:
                throw new IllegalArgumentException("Unrecognized ID type: " + this);
        }
    }

}