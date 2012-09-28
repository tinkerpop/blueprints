package com.tinkerpop.blueprints.util.wrappers.batch.loader;

import java.util.Map;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public interface Triple {
    
    public Object getOutVertexId();
    
    public String getType();
    
    public Map<String,Object> getProperties();

    public boolean isEdge();

    public boolean isProperty();
    
}
