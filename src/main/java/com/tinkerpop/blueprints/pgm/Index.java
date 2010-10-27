package com.tinkerpop.blueprints.pgm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Index<T extends Element> {

    public String getIndexName();

    public Class<T> getIndexClass();

    public void put(String key, Object value, T object);

    public Iterable<T> get(String key, Object value);

    public void remove(String key, Object value, T object);

}
