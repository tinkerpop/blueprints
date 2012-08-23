package com.tinkerpop.blueprints.impls.tg.properties;

import com.tinkerpop.blueprints.Element;

import java.util.Set;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public interface PropertyContainer {

    public Object get(String key);
    
    public PropertyContainer setProperty(String key, Object value);
    
    public Set<String> getPropertyKeys();
    
    public Iterable<Property> getProperties();
    
    public PropertyContainer removeProperty(String key);

    public void clear();

}
