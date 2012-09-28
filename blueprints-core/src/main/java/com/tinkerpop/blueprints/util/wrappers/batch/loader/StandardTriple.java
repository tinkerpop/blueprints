package com.tinkerpop.blueprints.util.wrappers.batch.loader;

import com.tinkerpop.blueprints.util.wrappers.batch.CompactMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

abstract class StandardTriple implements Triple {
    
    private static final Map<String,Object> EMPTY_PROPERTIES = Collections.unmodifiableMap(new HashMap<String,Object>());
    
    private final Object outVertexId;
    private final String type;
    private final Object inObject;
    private final Map<String,Object> properties;

    StandardTriple(final Object outVertexId, final String type, final Object inObject) {
        this(outVertexId,type,inObject,null);
    }

    StandardTriple(final Object outVertexId, final String type, final Object inObject,
                   final String[] propertykeys, final Object[] propertyValues) {
        this(outVertexId,type,inObject, CompactMap.of(propertykeys,propertyValues));
    }
    
    StandardTriple(final Object outVertexId, final String type, final Object inObject, final Map<String,Object> properties) {
        if (outVertexId==null) throw new IllegalArgumentException("out vertex id cannot be null");
        if (type==null || type.isEmpty()) throw new IllegalArgumentException("Must specify a type");
        if (inObject==null) throw new IllegalArgumentException("in object cannot be null");

        this.outVertexId=outVertexId;
        this.type=type;
        this.inObject=inObject;
        if (properties==null || properties.isEmpty()) this.properties=EMPTY_PROPERTIES;
        else this.properties=properties;
    }
    
    @Override
    public Object getOutVertexId() {
        return outVertexId;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public boolean isProperty() {
        return !isEdge();
    }
    
    Object getInObject() {
        return inObject;
    }

}
