package com.tinkerpop.blueprints.odm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Document<T> {

    public Object put(String key, Object value);

    public Object get(String key);

    public Iterable<String> keys();

    public T getRawObject();
}
