package com.tinkerpop.blueprints.util.wrappers.batch;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.util.wrappers.batch.vertexcache.*;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public enum IDType {
    
    OBJECT, NUMBER, STRING, URL;
    
    VertexCache getVertexCache(Graph g) {
        switch(this) {
            case OBJECT: return new ObjectIDVertexCache(g);
            case NUMBER: return new LongIDVertexCache(g);
            case STRING: return new StringIDVertexCache(g);
            case URL: return new StringIDVertexCache(g,new URLCompression());
            default: throw new IllegalArgumentException("Unrecognized ID type: " + this);
        }
    }
    
}
