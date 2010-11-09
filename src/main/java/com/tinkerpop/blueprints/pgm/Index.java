package com.tinkerpop.blueprints.pgm;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public interface Index<T extends Element> {

    public static final String VERTICES = "vertices";
    public static final String EDGES = "edges";

    enum Type {
        MANUAL, AUTOMATIC
    }

    public String getIndexName();

    public Class<T> getIndexClass();

    public Type getIndexType();

    public void put(String key, Object value, T element);

    public Iterable<T> get(String key, Object value);

    public void remove(String key, Object value, T element);
}
