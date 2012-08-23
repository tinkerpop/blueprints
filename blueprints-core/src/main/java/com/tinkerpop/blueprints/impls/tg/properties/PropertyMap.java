package com.tinkerpop.blueprints.impls.tg.properties;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * (c) Matthias Broecheler (me@matthiasb.com)
 */

public class PropertyMap extends HashMap<String,Object> implements PropertyContainer {

    private final boolean internStrings = true;

    public PropertyMap() {
        super();
    }

    public PropertyMap(int capacity) {
        super(capacity);
    }

    @Override
    public Object get(final String key) {
        if (key==null) throw new IllegalArgumentException("Key may not be null");
        return super.get(key);
    }

    @Override
    public PropertyContainer setProperty(final String key, final Object value) {
        if (key==null) throw new IllegalArgumentException("Key may not be null");
        //if (value==null) throw new IllegalArgumentException("Value may not be null");
        super.put(key.intern(),value);
        return this;
    }

    @Override
    public Set<String> getPropertyKeys() {
        return super.keySet();
    }

    @Override
    public Iterable<Property> getProperties() {
        return new Iterable<Property>() {
            @Override
            public Iterator<Property> iterator() {

                return new Iterator<Property>() {

                    final Iterator<Map.Entry<String,Object>> iterator = entrySet().iterator();
                    
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Property next() {
                        Map.Entry<String,Object> entry = iterator.next();
                        return new Property(entry.getKey(),entry.getValue());
                    }

                    @Override
                    public void remove() {
                        iterator.remove();
                    }
                };
            }
        };
    }

    @Override
    public PropertyContainer removeProperty(final String key) {
        if (key==null) throw new IllegalArgumentException("Key may not be null");
        super.remove(key);
        return this;
    }

    @Override
    public void clear() {
        super.clear();
    }

}
