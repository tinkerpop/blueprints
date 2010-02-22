package com.tinkerpop.blueprints.odm;

import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Document<T> {

    public Object put(String key, Object value);

    public Object get(String key);

    public Iterable<String> keys();

    public Map<String, Object> asMap();

    public T getRawObject();
}
