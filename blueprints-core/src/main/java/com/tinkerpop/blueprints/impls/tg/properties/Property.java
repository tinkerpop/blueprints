package com.tinkerpop.blueprints.impls.tg.properties;

import java.io.Serializable;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class Property implements Serializable {
    
    private final String key;
    private final Object value;
    
    public Property(final String key, final Object value) {
        this.key=key;
        this.value=value;
    }
    
    public String getKey() {
        return key;
    }
    
    public Object getValue() {
        return value;
    }
    
}
